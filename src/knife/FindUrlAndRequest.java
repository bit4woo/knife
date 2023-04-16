package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.Utils;
import burp.threadRequester;


public class FindUrlAndRequest extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public FindUrlAndRequest(BurpExtender burp){
		this.setText("^_^ Find URL And Request");
		this.addActionListener(new FindUrl_Action(burp,burp.invocation));
	}
	
	public static void main(String[] args) {
		String url = "./abac/aaa.jpg";
		if (url.startsWith("./")) {
			url = url.replaceFirst("\\./", "");
		}
		System.out.println(url);
	}
}

class FindUrl_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public FindUrl_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
		this.stdout = BurpExtender.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Runnable requestRunner = new Runnable() {
			private String referUrl;

			@Override
			public void run() {
				try{
					IHttpRequestResponse[] messages = invocation.getSelectedMessages();
					HelperPlus getter = new HelperPlus(helpers);
					if (messages == null || messages.length <=0) {
						return;
					}

					BlockingQueue<String> inputQueue = new LinkedBlockingQueue<String>();

					try {
						//一般都是对一个JS文件进行路径提取，不需要循环
						IHttpRequestResponse message = messages[0];
						byte[] respBody = HelperPlus.getBody(false, message);
						if (null == respBody) {
							return;
						}
						String body = new String(respBody);
						List<String> urls = Utils.grepURL(body);
						Set<String> baseUrls = getBaseURL(urls);

						referUrl = getter.getHeaderValueOf(true,message,"Referer");
						if (referUrl != null) {
							baseUrls.add(referUrl);
						}

						String fullUrl = getter.getFullURL(message).toString();
						baseUrls.add(fullUrl);

						String baseurl = choseAndEditBaseURL(baseUrls);

						if (null==baseurl) {
							return;
						}

						for (String url:urls) {
							if (Utils.uselessExtension(url)) {
								continue;
							}
							if (!url.startsWith("http://") && !url.startsWith("https://")) {
								if (url.startsWith("/")) {
									url = url.replaceFirst("/", "");
								}
								if (url.startsWith("./")) {
									url = url.replaceFirst("\\./", "");
								}
								url = baseurl+url; //baseurl统一以“/”结尾；url统一删除“/”的开头
								inputQueue.put(url);
							}
						}
					} catch (Exception e) {
						e.printStackTrace(BurpExtender.getStderr());
					}

					doRequest(inputQueue,referUrl);
				}
				catch (Exception e1)
				{
					e1.printStackTrace(stderr);
				}
			}
		};
		new Thread(requestRunner).start();
	}

	/**
	 * 多线程执行请求
	 * @param inputQueue
	 */
	public void doRequest(BlockingQueue<String> inputQueue,String referUrl) {
		String proxyHost = BurpExtender.getProxyHost();
		int proxyPort = BurpExtender.getProxyPort();

		if (proxyHost == null || proxyPort == -1) {
			return;
		}

		int max = threadNumberShouldUse(inputQueue.size());

		for (int i=0;i<=max;i++) {
			threadRequester requester = new threadRequester(inputQueue,proxyHost,proxyPort,referUrl,i);
			requester.start();
		}
	}

	/**
	 * 根据已有的域名梳理，预估应该使用的线程数
	 * 假设1个任务需要1秒钟。线程数在1-100之间，如何选择线程数使用最小的时间？
	 * @param domains
	 * @return
	 */
	public static int threadNumberShouldUse(int domainNum) {

		int tmp = (int) Math.sqrt(domainNum);
		if (tmp <=1) {
			return 1;
		}else if(tmp>=10) {
			return 10;
		}else {
			return tmp;
		}
	}

	public static Set<String> getBaseURL(List<String> urls) {
		Set<String> baseURLs = new HashSet<String>();
		for (String tmpurl:urls) {
			//这部分提取的是含有协议头的完整URL地址
			if (tmpurl.toLowerCase().startsWith("http://")
					||tmpurl.toLowerCase().startsWith("https://")){
				baseURLs.add(tmpurl);
			}
		}
		return baseURLs;
	}

	public static String choseAndEditBaseURL(Set<String> inputs) {

		ArrayList<String> tmpList = new ArrayList<String>(inputs);
		Collections.sort(tmpList);
		int n = inputs.size()+1;
		String[] possibleValues = new String[n];

		// Copying contents of domains to arr[]
		System.arraycopy(tmpList.toArray(), 0, possibleValues, 0, n-1);
		possibleValues[n-1] = "Let Me Input";

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
				baseUrl = baseUrl.trim()+"/";
			}
			return baseUrl.trim();
		}
		return selectedValue;
	}
}
