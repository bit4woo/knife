package base;

import java.net.MalformedURLException;
import java.net.URL;

import com.github.kevinsawicki.http.HttpRequest;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IHttpService;

public class RequestTask {

	String url;
	RequestType requestType;

	public RequestTask(String url,RequestType requestType) {
		this.url = url;
		this.requestType = requestType;
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

	public void sendRequest(String proxyHost,int proxyPort,String referUrl) {

		if (referUrl ==null || referUrl.equals("")) {
			referUrl = url;
		}
		System.out.println("send request:"+url+"  using proxy:"+proxyHost+":"+proxyPort);
		if (requestType == RequestType.GET) {
			doGetReq(url,proxyHost,proxyPort,referUrl);
		}
		if (requestType == RequestType.POST) {
			doPostReq(url,proxyHost,proxyPort,referUrl);
		}
		if (requestType == RequestType.JSON) {
			doPostJsonReq(url,proxyHost,proxyPort,referUrl);
		}
	}


	@Deprecated
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

}
