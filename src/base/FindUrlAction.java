package base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

	public static final List<String> blackHostList = TextUtils.textToLines("ns.adobe.com\r\n"
			+ "schemas.microsoft.com\r\n"
			+ "iptc.org\r\n"
			+ "jmespath.org\r\n"
			+ "json-schema.org\r\n"
			+ "registry.npmjs.org\r\n"
			+ "openoffice.org\r\n"
			+ "schemas.openxmlformats.org\r\n"
			+ "sheetjs.openxmlformats.org\r\n"
			+ "www.w3.org");

	public static final List<String> blackPath = TextUtils.textToLines("application/zip\r\n"
			+ "application/x-www-form-urlencoded\r\n"
			+ "application/x-mso\r\n"
			+ "application/xml\r\n"
			+ "application/vnd.\r\n"
			+ "application/pdf\r\n"
			+ "application/octet-stream\r\n"
			+ "application/json\r\n"
			+ "text/css\r\n"
			+ "text/html\r\n"
			+ "text/plain\r\n"
			+ "image/pdf\r\n"
			+ "image/x-wmf\r\n"
			+ "image/x-emf\r\n"
			+ "image/tiff\r\n"
			+ "image/png\r\n"
			+ "image/jpeg\r\n"
			+ "image/gif\r\n"
			+ "image/bmp");


	public static Proxy CurrentProxy;
	public static HashMap<String, String> httpServiceBaseUrlMap = new HashMap<>();

	public FindUrlAction(BurpExtender burp, IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation = invocation;
		this.helpers = BurpExtender.helpers;
		this.callbacks = BurpExtender.callbacks;
	}

	public static List<String> buildUrls(String baseurl, List<String> urlPath){
		List<String> result = new ArrayList<>();

		for (String url : urlPath) {
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				if (url.startsWith("/")) {
					url = url.replaceFirst("/", "");
				}
				if (url.startsWith("./")) {
					url = url.replaceFirst("\\./", "");
				}
				url = baseurl + url; //baseurl统一以“/”结尾；url统一删除“/”的开头
				result.add(url);
			}
		}
		return result;
	}

	public static void doSendRequest(List<String> full_urls, String refererToUse) {
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
					}
				}
			} catch (Exception e) {
				e.printStackTrace(BurpExtender.getStderr());
			}

			doRequest(inputQueue, refererToUse);
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
				String targetBaseUrl = getTargetSiteBaseUrl(messages[0]);

				List<String> urls = FindAllUrlsOfTarget(targetBaseUrl);

				String baseurl = choseAndEditBaseURL(urls);

				if (null == baseurl) {
					return;
				}

				httpServiceBaseUrlMap.put(targetBaseUrl, baseurl);

				urls = choseURLPath(urls);
				if (urls.size() == 0) return;
				List<String> full_urls = buildUrls(baseurl, urls);
				doSendRequest(full_urls, targetBaseUrl);
			}
		};
		new Thread(requestRunner).start();
	}

	/**
	 * 根据当前web的baseUrl找JS，特征就是referer以它开头
	 *
	 * @return
	 */
	public static List<String> FindAllUrlsOfTarget(IHttpService httpService, byte[] request, byte[] response) {
		String targetBaseUrl = getTargetSiteBaseUrl(httpService, request);
		return FindAllUrlsOfTarget(targetBaseUrl);
	}


	public static List<String> FindAllUrlsOfTarget(String targetBaseUrl) {
		List<String> urls = new ArrayList<>();
		urls.add(targetBaseUrl);
		//List<String> urls = findUrls(response);
		//siteMap中应该也会包含这个请求的

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

			String referUrl = getter.getHeaderValueOf(true, item, "Referer");
			//JS请求必然有referer，js.map请求则没有referer，首页的.html请求也没有
			if (url.toString().toLowerCase().endsWith(".js")) {
				if (referUrl == null) {
					continue;
				}
				if (referUrl.toLowerCase().startsWith(targetBaseUrl.toLowerCase())) {
					urls.addAll(findUrls(item.getResponse()));
				}
			}
			//没必要处理js.map。
			if (url.toString().toLowerCase().endsWith(".js.map")) {

			}

			if (!url.toString().toLowerCase().endsWith(".html")) {
				if (referUrl == null) {
					if (url.toString().toLowerCase().startsWith(targetBaseUrl.toLowerCase())) {
						urls.addAll(findUrls(item.getResponse()));
					}
				} else {
					if (referUrl.toLowerCase().startsWith(targetBaseUrl.toLowerCase())) {
						urls.addAll(findUrls(item.getResponse()));
					}
				}
			}
		}
		return urls;
	}


	/**
	 * 一个数据包，确定它的【来源】
	 *
	 * @param message
	 * @return
	 */
	public static String getTargetSiteBaseUrl(IHttpRequestResponse message) {
		return getTargetSiteBaseUrl(message.getHttpService(), message.getRequest());
	}


	public static String getTargetSiteBaseUrl(IHttpService httpService, byte[] request) {
		HelperPlus getter = BurpExtender.getHelperPlus();

		String current_referUrl = getter.getHeaderValueOf(true, request, "Referer");
		String current_fullUrl = getter.getFullURL(httpService, request).toString();

		if (current_referUrl != null) {
			//认为当前数据包是前端触发的
			return UrlUtils.getBaseUrl(current_referUrl);
		} else {
			//认为其是当前数据包是浏览器地址栏访问直接触发的
			return UrlUtils.getBaseUrl(current_fullUrl);
		}
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
		List<String> urls = new ArrayList<>();
		if (StringUtils.isEmpty(content)) {
			return urls;
		}

		content = TextUtils.decodeAll(content);
		urls.addAll(UrlUtils.grepUrlsWithProtocol(content));
		urls.addAll(UrlUtils.grepUrlPathNotStartWithSlashInQuotes(content));
		urls.addAll(UrlUtils.grepUrlsInQuotes(content));
		urls = cleanUrls(urls);

		return urls;
	}

	/**
	 * 多线程执行请求
	 *
	 * @param inputQueue
	 */
	public static void doRequest(BlockingQueue<RequestTask> inputQueue, String referUrl) {
		if (CurrentProxy == null) {
			CurrentProxy = Proxy.inputProxy();
		}
		if (CurrentProxy == null) {
			return;
		}

		int max = threadNumberShouldUse(inputQueue.size());

		for (int i = 0; i <= max; i++) {
			threadRequester requester = new threadRequester(inputQueue, CurrentProxy.getHost(), CurrentProxy.getPort(), referUrl, i);
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

	public static List<String> findPossibleBaseURL(List<String> urls) {
		List<String> baseURLs = new ArrayList<>();
		for (String tmpurl : urls) {
			//这部分提取的是含有协议头的完整URL地址
			if (tmpurl.toLowerCase().startsWith("http://")
					|| tmpurl.toLowerCase().startsWith("https://")) {
				if (!baseURLs.contains(tmpurl)) {
					baseURLs.add(tmpurl);
				}
			}
		}
		return baseURLs;
	}


	public static String choseAndEditBaseURL(List<String> inputs) {

		Collections.sort(inputs);
		inputs = findPossibleBaseURL(inputs);

		int n = inputs.size() + 1;
		String[] possibleValues = new String[n];

		// Copying contents of domains to arr[]
		System.arraycopy(inputs.toArray(), 0, possibleValues, 0, n - 1);
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
			if (!baseUrl.endsWith("/")) {
				baseUrl = baseUrl.trim() + "/";
			}
			return baseUrl.trim();
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
			}
			if (TextUtils.containsAny(urlItem, blackPath, false)) {
				it.remove();
			}
			try {
				String host = new URL(urlItem).getHost();
				if (blackHostList.contains(host)) {
					it.remove();
				}
			} catch (Exception E) {
				continue;
			}
		}
		return urls;
	}
}
