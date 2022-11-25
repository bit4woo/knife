package burp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import com.github.kevinsawicki.http.HttpRequest;

public class threadRequester extends Thread {
	private final BlockingQueue<String> inputQueue;
	private String proxyHost;
	private int proxyPort;
	private String referUrl;

	public threadRequester(BlockingQueue<String> inputQueue,String proxyHost,int proxyPort,String referUrl,int threadNo) {
		this.inputQueue = inputQueue;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.referUrl = referUrl;
		this.setName(this.getClass().getName()+threadNo);
	}

	@Override
	public void run() {
		while(true){
			try {
				if (inputQueue.isEmpty() ) {
					break;
				}
				if (Thread.interrupted()){//没有起作用！
					break;
				}

				String url = inputQueue.take();
				try {
					sendRequest(url,proxyHost,proxyPort,referUrl);
				} catch (Exception e) {
					e.printStackTrace(BurpExtender.getStderr());
					sendRequestWithBurpMethod(url);
				}
			} catch (Exception error) {
				error.printStackTrace(BurpExtender.getStderr());
			}
		}
	}

	public static void doGetReq(String url,String proxyHost,int proxyPort,String referUrl) {
		HttpRequest request = HttpRequest.get(url);
		//Configure proxy
		request.useProxy(proxyHost, proxyPort);
		request.header("Referer", referUrl);

		//Accept all certificates
		request.trustAllCerts();
		//Accept all hostnames
		request.trustAllHosts();

		request.code();
	}

	public static void doPostReq(String url,String proxyHost,int proxyPort,String referUrl) 
	{
		HttpRequest postRequest = HttpRequest.post(url);
		//Configure proxy
		postRequest.useProxy(proxyHost, proxyPort);
		postRequest.header("Referer", referUrl);
		//Accept all certificates
		postRequest.trustAllCerts();
		//Accept all hostnames
		postRequest.trustAllHosts();

		postRequest.send("test=test");
		postRequest.code();
	}

	public static void doPostJsonReq(String url,String proxyHost,int proxyPort,String referUrl) 
	{
		HttpRequest postRequest = HttpRequest.post(url);
		//Configure proxy
		postRequest.useProxy(proxyHost, proxyPort);
		postRequest.header("Referer", referUrl);
		postRequest.header("Content-Type", "application/json");
		//Accept all certificates
		postRequest.trustAllCerts();
		//Accept all hostnames
		postRequest.trustAllHosts();


		postRequest.send("{}");
		postRequest.code();
	}

	public static void sendRequest(String url,String proxyHost,int proxyPort,String referUrl) {

		if (referUrl ==null || referUrl.equals("")) {
			referUrl = url;
		}
		System.out.println("send request:"+url+"  using proxy:"+proxyHost+":"+proxyPort);
		doPostJsonReq(url,proxyHost,proxyPort,referUrl);
		doPostReq(url,proxyHost,proxyPort,referUrl);
		doGetReq(url,proxyHost,proxyPort,referUrl);
	}

	public static void sendRequestWithBurpMethod(String url) {
		URL tmpUrl;
		try {
			tmpUrl = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		}

		IBurpExtenderCallbacks callbacks = BurpExtender.getCallbacks();

		byte[] req = callbacks.getHelpers().buildHttpRequest(tmpUrl);
		HelperPlus hp = new HelperPlus(callbacks.getHelpers());
		req = hp.addOrUpdateHeader(true, req, "X-sent-by-knife", "X-sent-by-knife");

		int port = tmpUrl.getPort() == -1? tmpUrl.getDefaultPort():tmpUrl.getPort();
		IHttpService service = callbacks.getHelpers().buildHttpService(tmpUrl.getHost(), port, tmpUrl.getProtocol());

		IHttpRequestResponse message = BurpExtender.getCallbacks().makeHttpRequest(service, req);
		message.setComment("Sent by Knife");//在logger中没有显示comment

		byte[] postReq = callbacks.getHelpers().toggleRequestMethod(req);
		IHttpRequestResponse message1 = BurpExtender.getCallbacks().makeHttpRequest(service, postReq);
		message.setComment("Sent by Knife");//在logger中没有显示comment
	}

	public static void main(String[] args) {
		doPostJsonReq("https://www.baidu.com","127.0.0.1",8080,"");
	}

}
