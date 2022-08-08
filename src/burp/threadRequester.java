package burp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.BlockingQueue;

import com.github.kevinsawicki.http.HttpRequest;

public class threadRequester extends Thread {
	private final BlockingQueue<String> inputQueue;
	private String proxyHost;
	private int proxyPort;

	public threadRequester(BlockingQueue<String> inputQueue,String proxyHost,int proxyPort,int threadNo) {
		this.inputQueue = inputQueue;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
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
					sendRequest(url,proxyHost,proxyPort);
				} catch (Exception e) {
					e.printStackTrace(BurpExtender.getStderr());
					sendRequestWithBurpMethod(url);
				}
			} catch (Exception error) {
				error.printStackTrace(BurpExtender.getStderr());
			}
		}
	}
	
	public static void sendRequest(String url,String proxyHost,int proxyPort) {
		HttpRequest request = HttpRequest.get(url);
		//Configure proxy
		request.useProxy(proxyHost, proxyPort);

		//Accept all certificates
		request.trustAllCerts();
		//Accept all hostnames
		request.trustAllHosts();

		request.code();


		HttpRequest postRequest = HttpRequest.post(url);
		//Configure proxy
		postRequest.useProxy(proxyHost, proxyPort);
		//Accept all certificates
		postRequest.trustAllCerts();
		//Accept all hostnames
		postRequest.trustAllHosts();

		postRequest.send("test=test");
		postRequest.code();

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
		message.setComment("Sent by Knife");
		
		byte[] postReq = callbacks.getHelpers().toggleRequestMethod(req);
		IHttpRequestResponse message1 = BurpExtender.getCallbacks().makeHttpRequest(service, postReq);
		message.setComment("Sent by Knife");
	}
	
	public static void main(String[] args) {
		sendRequest("http://127.0.0.1:8080","127.0.0.1",8080);
	}

}
