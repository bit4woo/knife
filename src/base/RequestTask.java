package base;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.github.kevinsawicki.http.HttpRequest;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IHttpService;

public class RequestTask {

	String url;
	RequestType requestType;
	
	static final String userAgentKey = "User-Agent";
	static final String userAgentValue = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:129.0) Gecko/20100101 Firefox/129.0";
	static final String RefererKey = "Referer";

	public RequestTask(String url,RequestType requestType) {
		this.url = url;
		this.requestType = requestType;
	}
	
	
	private static HttpRequest configHttpRequest(HttpRequest request,String proxyHost,int proxyPort,HashMap<String,String> headers) {

		//Configure proxy
		request.useProxy(proxyHost, proxyPort);
		
		for (String key:headers.keySet()) {
			String value =headers.get(key); 
			request.header(key, value);
		}
		
		if (!headers.keySet().contains(userAgentKey)) {
			request.header(userAgentKey, userAgentValue);
		}
		
		//Accept all certificates
		request.trustAllCerts();
		//Accept all hostnames
		request.trustAllHosts();

		return request;
	}
	

	//TODO 使用已有请求的header？尤其是cookie
	public static void doGetReq(String url,String proxyHost,int proxyPort,HashMap<String,String> headers) {
		HttpRequest request = HttpRequest.get(url);
		request = configHttpRequest(request,proxyHost,proxyPort,headers);
		request.code();
	}
	

	public static void doPostReq(String url,String proxyHost,int proxyPort,HashMap<String,String> headers) 
	{
		HttpRequest postRequest = HttpRequest.post(url);
		postRequest = configHttpRequest(postRequest,proxyHost,proxyPort,headers);

		postRequest.send("test=test");
		postRequest.code();
	}

	public static void doPostJsonReq(String url,String proxyHost,int proxyPort,HashMap<String,String> headers) 
	{
		HttpRequest postRequest = HttpRequest.post(url);
		
		postRequest = configHttpRequest(postRequest,proxyHost,proxyPort,headers);
		
	
		postRequest.header("Content-Type", "application/json");
		postRequest.send("{}");
		postRequest.code();
	}

	public void sendRequest(String proxyHost,int proxyPort,HashMap<String,String> headers) {

		if (!headers.keySet().contains(RefererKey)) {
			headers.put(RefererKey, url);
		}
		System.out.println("send request:"+url+"  using proxy:"+proxyHost+":"+proxyPort);
		if (requestType == RequestType.GET) {
			doGetReq(url,proxyHost,proxyPort,headers);
		}
		if (requestType == RequestType.POST) {
			doPostReq(url,proxyHost,proxyPort,headers);
		}
		if (requestType == RequestType.JSON) {
			doPostJsonReq(url,proxyHost,proxyPort,headers);
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
