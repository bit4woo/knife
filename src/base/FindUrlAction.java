package base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.SwingUtils;
import com.bit4woo.utilbox.utils.TextUtils;
import com.bit4woo.utilbox.utils.UrlUtils;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.threadRequester;

public class FindUrlAction implements ActionListener {
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public static final List<String> blackHostList = Arrays.asList(
			"ns.adobe.com", "schemas.microsoft.com", "iptc.org", "jmespath.org",
			"json-schema.org", "registry.npmjs.org", "openoffice.org",
			"schemas.openxmlformats.org", "sheetjs.openxmlformats.org", "www.w3.org",
			"w3.org", "xml.org", "purl.org", "unpkg.com", "cdn.jsdelivr.net",
			"cdnjs.cloudflare.com", "esm.sh", "fonts.googleapis.com", "fonts.gstatic.com",
			"code.jquery.com", "ajax.googleapis.com", "maxcdn.bootstrapcdn.com",
			"google-analytics.com", "googletagmanager.com", "doubleclick.net", "hotjar.com");

	public static final List<String> blackPath = Arrays.asList(
			"node_modules", "/dist/", "/build/", "/vendor/", "/assets/", "/static/",
			"/chunks/", ".map", "/locale/", "/locales/", "/lang/", "/i18n/",
			"/fonts/", "/icons/", "/favicon");

public static Proxy CurrentProxy;
	public static HashMap<String, String> httpServiceBaseUrlMap = new HashMap<>();

	public FindUrlAction(BurpExtender burp, IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation = invocation;
		this.helpers = BurpExtender.helpers;
		this.callbacks = BurpExtender.callbacks;
	}

	public static List<String> buildUrls(String baseurl, List<String> urlPath) {
		List<String> result = new ArrayList<>();
		if (StringUtils.isBlank(baseurl)) {
			return result;
		}
		//兜底：确保baseurl以"/"结尾，避免拼接出 http://hostapi/xxx 这类错误URL
		baseurl = baseurl.trim();
		if (!baseurl.endsWith("/")) {
			baseurl = baseurl + "/";
		}

		for (String url : urlPath) {
			if (StringUtils.isBlank(url)) {
				continue;
			}
			String lower = url.toLowerCase();
			if (lower.startsWith("http://") || lower.startsWith("https://")) {
				result.add(url);
				continue;
			}
			//协议相对URL：//host/path，补上baseurl的协议
			if (url.startsWith("//")) {
				String protocol = baseurl.toLowerCase().startsWith("https://") ? "https:" : "http:";
				result.add(protocol + url);
				continue;
			}
			String path = url;
			//去掉开头的./和../（相对baseurl根目录无意义）
			while (path.startsWith("./") || path.startsWith("../")) {
				path = path.substring(path.startsWith("./") ? 2 : 3);
			}
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			url = baseurl + path; //baseurl统一以"/"结尾；url统一删除"/"的开头
			result.add(url);
		}
		return result;
	}

	public static void doSendRequest(List<String> full_urls, HashMap<String, String> headers) {
		try {
			BlockingQueue<RequestTask> inputQueue = new LinkedBlockingQueue<>();

			try {
				for (String url : full_urls) {
					inputQueue.put(new RequestTask(url, RequestType.GET));

					if (url.toLowerCase().endsWith(".js") || url.toLowerCase().endsWith(".html")) {
						//不严谨，TODO应该判断path
					} else {
						inputQueue.put(new RequestTask(url, RequestType.POST));
						inputQueue.put(new RequestTask(url, RequestType.JSON));
						inputQueue.put(new RequestTask(url, RequestType.XML));
					}
				}
			} catch (Exception e) {
				e.printStackTrace(BurpExtender.getStderr());
			}
			doRequest(inputQueue, headers);
		} catch (Exception e1) {
			e1.printStackTrace(BurpExtender.getStderr());
		}
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Runnable requestRunner = new Runnable() {
			@Override
			public void run() {
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();
				if (messages == null || messages.length == 0) {
					return;
				}
				String originUrl = getOriginUrlOfMessage(messages[0]);
				String referUrl = getReferUrlOfMessage(messages[0]);
				String currentUrl = getFullUrlOfMessage(messages[0]);

				List<String> urls = FindAllUrlsOfTarget(originUrl);

				String baseurl = choseAndEditBaseURL(urls, referUrl, currentUrl);

				if (null == baseurl) {
					return;
				}

				httpServiceBaseUrlMap.put(originUrl, baseurl);

				urls = choseURLPath(urls);
				if (urls.size() == 0) return;
				List<String> full_urls = buildUrls(baseurl, urls);
				HashMap<String, String> headers = new HashMap<>();
				headers.put("Referer", originUrl);
				doSendRequest(full_urls, headers);
			}
		};
		new Thread(requestRunner).start();
	}

	/**
	 * 根据当前web的originUrl找JS，特征就是referer以它开头
	 *
	 * @return
	 */
	public static List<String> FindAllUrlsOfTarget(IHttpService httpService, byte[] request, byte[] response) {
		String originUrl = getOriginUrlOfMessage(httpService, request);
		return FindAllUrlsOfTarget(originUrl);
	}


	public static List<String> FindAllUrlsOfTarget(String originUrl) {
		List<String> urls = new ArrayList<>();
		if (StringUtils.isEmpty(originUrl)) {
			return urls;
		}
		String originLower = originUrl.toLowerCase();

		HelperPlus getter = BurpExtender.getHelperPlus();
		IHttpRequestResponse[] messages = BurpExtender.getCallbacks().getSiteMap(null);
		for (IHttpRequestResponse item : messages) {
			if (item == null || item.getResponse() == null) {
				continue;
			}
			int code = getter.getStatusCode(item);
			if (code != 200) {
				continue;
			}

			URL url = getter.getFullURL(item);
			if (url == null) {
				continue;
			}
			String urlStr = url.toString();
			String urlLower = urlStr.toLowerCase();

			//首页的.html请求通常没有referer，价值低，不处理
			if (urlLower.endsWith(".html")) {
				continue;
			}

			String referUrl = getter.getHeaderValueOf(true, item, "Referer");
			boolean belongToTarget;
			if (referUrl != null) {
				//有referer：referer属于目标才算
				belongToTarget = referUrl.toLowerCase().startsWith(originLower);
			} else {
				//无referer：js请求必然有referer，直接跳过；其他文件看URL本身是否属于目标
				if (urlLower.endsWith(".js")) {
					continue;
				}
				belongToTarget = urlLower.startsWith(originLower);
			}

			if (belongToTarget) {
				urls.addAll(findUrls(item.getResponse()));
			}
		}

		Collections.sort(urls);
		urls.add(0, originUrl);//把orginUrl放在最前面，它是baseUrl的概率比较高
		return urls;
	}


	/**
	 * 一个数据包，确定它的【来源】
	 *
	 * @param message
	 * @return
	 */
	public static String getOriginUrlOfMessage(IHttpRequestResponse message) {
		return getOriginUrlOfMessage(message.getHttpService(), message.getRequest());
	}

	public static String getReferUrlOfMessage(IHttpRequestResponse message) {
		return getReferUrlOfMessage(message.getHttpService(), message.getRequest());
	}

	public static String getFullUrlOfMessage(IHttpRequestResponse message) {
		return getFullUrlOfMessage(message.getHttpService(), message.getRequest());
	}

	/**
	 * 获取当前数据包的来源URL(OriginUrl),和请求包中的origin header是一个概念
	 *
	 * @param httpService
	 * @param request
	 * @return
	 */
	public static String getOriginUrlOfMessage(IHttpService httpService, byte[] request) {
		HelperPlus getter = BurpExtender.getHelperPlus();

		String current_referUrl = getter.getHeaderValueOf(true, request, "Referer");
		if (current_referUrl != null) {
			//认为当前数据包是前端触发的
			return UrlUtils.getBaseUrl(current_referUrl);
		} else {
			//认为其是当前数据包是浏览器地址栏访问直接触发的
			URL fullUrl = getter.getFullURL(httpService, request);
			if (fullUrl == null) {
				return null;
			}
			return UrlUtils.getBaseUrl(fullUrl.toString());
		}
	}

	public static String getReferUrlOfMessage(IHttpService httpService, byte[] request) {
		HelperPlus getter = BurpExtender.getHelperPlus();

		String current_referUrl = getter.getHeaderValueOf(true, request, "Referer");
		return current_referUrl;
	}


	public static String getFullUrlOfMessage(IHttpService httpService, byte[] request) {
		HelperPlus getter = BurpExtender.getHelperPlus();

		URL fullUrl = getter.getFullURL(httpService, request);
		if (fullUrl == null) {
			return null;
		}
		return fullUrl.toString();
	}

	public static List<String> findUrls(byte[] content) {
		List<String> urls = new ArrayList<>();

		if (content == null) {
			return urls;
		} else {
			return findUrls(new String(content));
		}
	}

	/**
	 * 在数据包中查找URL
	 *
	 * @param content
	 * @return
	 */
	public static List<String> findUrls(String content) {
		if (StringUtils.isEmpty(content)) {
			return new ArrayList<>();
		}

		content = TextUtils.decodeAll(content);
		//三个提取器合并去重（保持顺序），避免重复项在cleanUrls中反复解析
		Set<String> urlSet = new LinkedHashSet<>();
		urlSet.addAll(UrlUtils.grepUrlsWithProtocol(content));
		urlSet.addAll(UrlUtils.grepUrlPathNotStartWithSlashInQuotes(content));
		urlSet.addAll(UrlUtils.grepUrlsInQuotes(content));

		return cleanUrls(new ArrayList<>(urlSet));
	}

	/**
	 * 多线程执行请求
	 *
	 * @param inputQueue
	 */
	public static void doRequest(BlockingQueue<RequestTask> inputQueue, HashMap<String, String> headers) {
		if (CurrentProxy == null) {
			CurrentProxy = Proxy.inputProxy();
		}
		if (CurrentProxy == null) {
			return;
		}

		int max = threadNumberShouldUse(inputQueue.size());

		for (int i = 0; i <= max; i++) {
			threadRequester requester = new threadRequester(inputQueue, CurrentProxy.getHost(), CurrentProxy.getPort(), headers, i);
			requester.start();
		}
	}

	/**
	 * 根据已有的域名梳理，预估应该使用的线程数
	 * 假设1个任务需要1秒钟。线程数在1-100之间，如何选择线程数使用最小的时间？
	 *
	 * @param domainNum
	 * @return
	 */
	public static int threadNumberShouldUse(int domainNum) {

		int tmp = (int) Math.sqrt(domainNum);
		if (tmp <= 1) {
			return 1;
		} else if (tmp >= 10) {
			return 10;
		} else {
			return tmp;
		}
	}

	public static List<String> findPossibleBaseURL(List<String> urls, String referUrl, String currentUrl) {
		String referHost = (referUrl == null) ? "" : UrlUtils.getHost(referUrl);
		String currentHost = (currentUrl == null) ? "" : UrlUtils.getHost(currentUrl);

		//只保留带协议的完整URL，用LinkedHashSet去重并保持顺序
		Set<String> baseURLs = new LinkedHashSet<>();
		for (String tmpurl : urls) {
			if (tmpurl == null) {
				continue;
			}
			String lower = tmpurl.toLowerCase();
			if (lower.startsWith("http://") || lower.startsWith("https://")) {
				baseURLs.add(tmpurl);
			}
		}

		List<String> result = new ArrayList<>(baseURLs);
		//同源（host与refer/current一致）的候选排前面，其余按字典序
		Collections.sort(result, new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				boolean aSame = isSameOrigin(a, referHost, currentHost);
				boolean bSame = isSameOrigin(b, referHost, currentHost);
				if (aSame != bSame) {
					return aSame ? -1 : 1;
				}
				return a.compareTo(b);
			}
		});
		return result;
	}

	private static boolean isSameOrigin(String url, String referHost, String currentHost) {
		String host = UrlUtils.getHost(url);
		return host.equalsIgnoreCase(referHost) || host.equalsIgnoreCase(currentHost);
	}

	/**
	 * @param inputs
	 * @param referUrl   当前数据包的refer
	 * @param currentUrl 当前查找到url path数据包的URL
	 * @return
	 */
	public static String choseAndEditBaseURL(List<String> inputs, String referUrl, String currentUrl) {
		inputs = findPossibleBaseURL(inputs, referUrl, currentUrl);

		Set<String> uniqueUrls = new LinkedHashSet<>();

		for (String url : inputs) {
		    if (url.length() >= 150 && url.contains("?")) {
		        url = url.substring(0, url.indexOf('?'));
		    }
		    uniqueUrls.add(url); // 自动去重
		}

		int n = uniqueUrls.size() + 1;
		String[] possibleValues = new String[n];

		int index = 0;
		for (String url : uniqueUrls) {
		    possibleValues[index++] = url;
		}
		
		possibleValues[n - 1] = "Let Me Input";

		String selectedValue = (String) JOptionPane.showInputDialog(null,
				"Chose Base URL", "Chose And Edit Base URL",
				JOptionPane.INFORMATION_MESSAGE, null,
				possibleValues, possibleValues[0]);
		if (null != selectedValue) {
			String baseUrl = JOptionPane.showInputDialog("Confirm The Base URL", selectedValue);
			if (baseUrl == null) {
				return null;
			}
			baseUrl = baseUrl.trim();
			if (!baseUrl.endsWith("/")) {
				baseUrl = baseUrl + "/";
			}
			return baseUrl;
		}
		return selectedValue;
	}


	public static List<String> choseURLPath(List<String> urls) {

		Collections.sort(urls);

		String text = SwingUtils.showTextAreaDialog(String.join(System.lineSeparator(), urls));
		if (StringUtils.isEmpty(text)) {
			return new ArrayList<String>();
		} else {
			return TextUtils.textToLines(text);
		}
	}


	public static List<String> cleanUrls(List<String> urls) {

		urls = TextUtils.deduplicate(urls);
		Iterator<String> it = urls.iterator();
		while (it.hasNext()) {
			String urlItem = it.next();
			if (UrlUtils.uselessExtension(urlItem)) {
				it.remove();
				continue;
			}
			if (TextUtils.containsAny(urlItem, blackPath, false)) {
				it.remove();
				continue;
			}
			try {
				String host = new URL(urlItem).getHost();
				if (host != null && blackHostList.contains(host.toLowerCase())) {
					it.remove();
				}
			} catch (Exception E) {
				continue;
			}
		}
		return urls;
	}

	/**
	 * 排除无价值的前端源码/构建产物URL，只保留有价值的目标接口路径。
	 * 注意：.css/.png/.jpg/.svg/.vue 等静态资源扩展名（无query时）已由 cleanUrls 的 uselessExtension 覆盖。
	 */
	public static List<String> removeJsUrl(List<String> urls) {
		urls = TextUtils.deduplicate(urls);
		Iterator<String> it = urls.iterator();
		while (it.hasNext()) {
			String urlItem = it.next();
			// 仅在判断中去除参数和片段
			String cleanUrl = urlItem.split("\\?")[0].split("#")[0];

			if (isFrontendSourceFile(cleanUrl)
					|| isFrontendSourcePath(cleanUrl)
					|| isLocalePath(cleanUrl)) {
				it.remove();
			}
		}
		return urls;
	}

	//前端源码文件扩展名（.vue 补 uselessExtension 无法处理的带query场景）
	private static boolean isFrontendSourceFile(String cleanUrl) {
		return cleanUrl.endsWith(".js")
				|| cleanUrl.endsWith(".jsx")
				|| cleanUrl.endsWith(".ts")
				|| cleanUrl.endsWith(".tsx")
				|| cleanUrl.endsWith(".vue")
				|| cleanUrl.endsWith(".scss")
				|| cleanUrl.endsWith(".less")
				|| cleanUrl.endsWith(".mjs")
				|| cleanUrl.endsWith(".cjs");
	}

	//前端源码/框架路径（./src/、./views/等）
	private static boolean isFrontendSourcePath(String cleanUrl) {
		return cleanUrl.startsWith("./src/")
				|| cleanUrl.startsWith("./views/")
				|| cleanUrl.startsWith("./components/")
				|| cleanUrl.startsWith("./pages/")
				|| cleanUrl.startsWith("./routes/")
				|| cleanUrl.startsWith("./store/")
				|| cleanUrl.startsWith("./router/");
	}

	//语言包路径（如./en-US、./zh-CN）
	private static boolean isLocalePath(String cleanUrl) {
		return cleanUrl.startsWith("./") && cleanUrl.contains("-");
	}

	public static void main(String[] args) {

	}
}
