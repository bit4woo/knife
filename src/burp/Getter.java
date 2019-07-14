package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

public class Getter {
	private static IExtensionHelpers helpers;
	private static String Header_Spliter = ": ";
	private PrintWriter stderr = new PrintWriter(BurpExtender.callbacks.getStderr(), true);

	public Getter(IExtensionHelpers helpers) {
		Getter.helpers = helpers;
	}

	public Getter(){
		Getter.helpers = BurpExtender.callbacks.getHelpers();
	}

	/*
	 * 获取header的字符串数组，是构造burp中请求需要的格式。
	 */
	public List<String> getHeaderList(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
		if(messageIsRequest) {
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
			return analyzeRequest.getHeaders();
		}else {
			IResponseInfo analyzeResponse = helpers.analyzeResponse(messageInfo.getResponse());
			return analyzeResponse.getHeaders();
		}
	}

	/*
	 * 获取所有headers，当做一个string看待。
	 * 主要用于判断是否包含某个特殊字符串
	 * List<String> getHeaders 调用toString()方法，得到如下格式：[111111, 2222]
	 * 就能满足上面的场景了
	 */
	public String getHeaderString(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
		List<String> headers =getHeaderList(messageIsRequest,messageInfo);
		StringBuilder headerString = new StringBuilder();
		for (String header : headers) {
			headerString.append(header);
		}
		return headerString.toString();
	}

	/*
	 * 获取header的map格式，key:value形式
	 * 这种方式可以用put函数轻松实现：如果有则update，如果无则add。
	 * ！！！注意：这个方法获取到的map，会少了协议头GET /cps.gec/limit/information.html HTTP/1.1
	 */
	public LinkedHashMap<String,String> getHeaderHashMap(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
		List<String> headers = getHeaderList(messageIsRequest,messageInfo);
		LinkedHashMap<String,String> result = new LinkedHashMap<String, String>();
		if (headers.size() <=0) return result;
		headers.remove(0);
		for (String header : headers) {
			try {
				String headerName = header.split(Header_Spliter, 2)[0];
				String headerValue = header.split(Header_Spliter, 2)[1];
				result.put(headerName, headerValue);
			} catch (Exception e) {
				try {
					String headerName = header.split(":", 2)[0];//这里的limit=2 表示分割成2份，否则referer可能别分成3份
					String headerValue = header.split(":", 2)[1];
					result.put(headerName, headerValue);
				}catch (Exception e1) {
					stderr.print("Error Header: "+header);
				}
			}
		}
		return result;
	}

	public String getHeaderFirstLine(boolean messageIsRequest,IHttpRequestResponse messageInfo){
		if(messageIsRequest) {
			return helpers.analyzeRequest(messageInfo).getHeaders().get(0);
		}else {
			return helpers.analyzeResponse(messageInfo.getResponse()).getHeaders().get(0);
		}
	}

	public List<String> HeaderMapToList(String firstline,LinkedHashMap<String,String> Headers){
		List<String> result = new ArrayList<String>();
		for (Entry<String,String> header:Headers.entrySet()) {
			String item = header.getKey()+": "+header.getValue();
			result.add(item);
		}
		result.add(0,firstline);
		return result;
	}

	/*
	 * 获取某个header的值，如果没有此header，返回null。
	 */
	public String getHeaderValueOf(boolean messageIsRequest,IHttpRequestResponse messageInfo, String headerName) {
		LinkedHashMap<String,String> headers = getHeaderHashMap(messageIsRequest,messageInfo);
		return headers.get(headerName);
	}


	public byte[] getBody(boolean messageIsRequest,IHttpRequestResponse messageInfo) {
		if (messageInfo == null){
			return null;
		}
		if(messageIsRequest) {
			if (messageInfo.getRequest() ==null) {
				return null;
			}
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
			int bodyOffset = analyzeRequest.getBodyOffset();
			byte[] byte_Request = messageInfo.getRequest();

			byte[] byte_body = Arrays.copyOfRange(byte_Request, bodyOffset, byte_Request.length);//not length-1
			//String body = new String(byte_body); //byte[] to String
			return byte_body;
		}else {
			if (messageInfo.getResponse() ==null) {
				return null;
			}
			IResponseInfo analyzeResponse = helpers.analyzeResponse(messageInfo.getResponse());
			int bodyOffset = analyzeResponse.getBodyOffset();
			byte[] byte_Request = messageInfo.getResponse();

			byte[] byte_body = Arrays.copyOfRange(byte_Request, bodyOffset, byte_Request.length);//not length-1
			return byte_body;
		}
	}
	
	public byte[] getBody(boolean messageIsRequest,byte[] messagebyte) {
		if (messagebyte == null){
			return null;
		}
		int bodyOffset=0;
		if(messageIsRequest) {
			IRequestInfo analyzeRequest = helpers.analyzeRequest(messagebyte);
			bodyOffset = analyzeRequest.getBodyOffset();
		}else {
			IResponseInfo analyzeResponse = helpers.analyzeResponse(messagebyte);
			bodyOffset = analyzeResponse.getBodyOffset();
		}
		
		byte[] byte_body = Arrays.copyOfRange(messagebyte, bodyOffset, messagebyte.length);//not length-1
		//String body = new String(byte_body); //byte[] to String
		return byte_body;
	}

	
	public String getShortUrl(IHttpRequestResponse messageInfo) {
		return messageInfo.getHttpService().toString();
	}

	public URL getURL(IHttpRequestResponse messageInfo){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getUrl();

		//callbacks.getHelpers().analyzeRequest(baseRequestResponse).getUrl();
	}

	public String getHost(IHttpRequestResponse messageInfo) {
		return messageInfo.getHttpService().getHost();
	}

	public short getStatusCode(IHttpRequestResponse messageInfo) {
		IResponseInfo analyzedResponse = helpers.analyzeResponse(messageInfo.getResponse());
		return analyzedResponse.getStatusCode();
	}

	public List<IParameter> getParas(IHttpRequestResponse messageInfo){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getParameters();
	}


	public String getHTTPBasicCredentials(IHttpRequestResponse messageInfo) throws Exception{
		String authHeader  = getHeaderValueOf(true, messageInfo, "Authorization").trim();
		String[] parts = authHeader.split("\\s");

		if (parts.length != 2)
			throw new Exception("Wrong number of HTTP Authorization header parts");

		if (!parts[0].equalsIgnoreCase("Basic"))
			throw new Exception("HTTP authentication must be Basic");

		return parts[1];
	}

	public static void main(String args[]) {
		String a= "xxxxx%s%bxxxxxxx";
		System.out.println(String.format(a, "111"));
	}
}
