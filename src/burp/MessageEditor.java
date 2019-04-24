package burp;

import java.io.PrintWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.util.*;
import java.util.Map.Entry;

/*
这个类在使用中有多线程的同步问题！！！！特别是插件对scanner开启的时候！！！
 */
@Deprecated
public class  MessageEditor{
    private static IExtensionHelpers helpers;
	private static IHttpRequestResponse messageInfo;
	private boolean messageIsRequest;
	private static String Header_Spliter = ": ";

	//包的组成部分
	private IHttpService service = null;
	//private List<String> headers;// headers = firstline+headerMap
	private String firstLineOfHeader = null;
	private LinkedHashMap<String,String> headerMap = null;//doesn't contain first line!!!
	//为了保证header的转换过程中的顺序，使用LinkedHashMap
	//http://www.cnblogs.com/csliwei/archive/2012/01/12/2320674.html
	private byte[] body = null;
	
	
	PrintWriter stderr = new PrintWriter(BurpExtender.callbacks.getStderr(), true);

    public MessageEditor(boolean messageIsRequest,IHttpRequestResponse messageInfo,IExtensionHelpers helpers) {
    	this.messageIsRequest = messageIsRequest;
    	MessageEditor.helpers = helpers;
    	MessageEditor.messageInfo = messageInfo;
		parser();
    }

    private void  parser(){
		synchronized (this){//避免其他组件修改数据包，比如scanner,但是实测无用啊！synchronized (messageInfo)也不行！！！
			if (messageInfo == null){
				return;
			}
			if(messageIsRequest) {
				IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
				service = messageInfo.getHttpService();

				//debug
				String messageaddr = messageInfo.toString();
				String firstRequest = new String(messageInfo.getRequest());
				int code =  System.identityHashCode(messageInfo);
				int bodyOffset = helpers.analyzeRequest(messageInfo).getBodyOffset();
				int requestLength = messageInfo.getRequest().length;


				List<String> headerList = analyzeRequest.getHeaders();

				this.firstLineOfHeader = headerList.get(0);
				headerList.remove(0);//remove first line

				this.headerMap = new LinkedHashMap<String, String>();

				for (String header : headerList) {
					try{
						String headerName = header.split(Header_Spliter, 0)[0];
						String headerValue = header.split(Header_Spliter, 0)[1];
						this.headerMap.put(headerName, headerValue);
					}catch (Exception e){
						String headerName = header.split(":", 0)[0];
						String headerValue = header.split(":", 0)[1];
						this.headerMap.put(headerName, headerValue);
						//stderr.println(header);
					}
				}

				byte[] byte_Request = messageInfo.getRequest();
				if (byte_Request ==null) {
					return;
				}
//				int bodyOffset = analyzeRequest.getBodyOffset();
//				int requestLength = byte_Request.length;

				
				//debug
				String messageaddr1 = messageInfo.toString();
				String firstRequest1 = new String(messageInfo.getRequest());
				int code1 = System.identityHashCode(messageInfo);
				int bodyOffset1 = helpers.analyzeRequest(messageInfo).getBodyOffset();
				int requestLength1 = messageInfo.getRequest().length;


				try {
					this.body = Arrays.copyOfRange(byte_Request, bodyOffset, requestLength);//not length-1
					//String body = new String(byte_body); //byte[] to String
				}catch (Exception e){
					stderr.println ("////////////////////////////////");
					stderr.println ("first: bodyOffset "+bodyOffset+" requestLength "+requestLength+" hashcode "+code+" messageaddr "+messageaddr);
					stderr.println (firstRequest);
					stderr.println ("\n");
					stderr.println ("second: bodyOffset "+bodyOffset1+" requestLength "+requestLength1+" hashcode "+code1+" messageaddr1 "+messageaddr1);
					stderr.println (firstRequest1);
					stderr.println ("////////////////////////////////\n\n");
				}


			}else {
				IResponseInfo analyzeResponse = helpers.analyzeResponse(messageInfo.getResponse());
				List<String> headerList = analyzeResponse.getHeaders();

				this.firstLineOfHeader = headerList.get(0);
				headerList.remove(0);//remove first line

				this.headerMap = new LinkedHashMap<String, String>();
				for (String header : headerList) {
					String headerName = header.split(Header_Spliter, 0)[0];
					String headerValue = header.split(Header_Spliter, 0)[1];
					this.headerMap.put(headerName, headerValue);
				}

				byte[] byte_Response = messageInfo.getResponse();
				if (byte_Response ==null) {
					return;
				}
				int bodyOffset = analyzeResponse.getBodyOffset();

				this.body = Arrays.copyOfRange(byte_Response, bodyOffset, byte_Response.length);//not length-1
			}
		}//sync
	}

    public String getFirstLineOfHeader() {
		return firstLineOfHeader;
	}

	public void setFirstLineOfHeader(String firstLineOfHeader) {
		this.firstLineOfHeader = firstLineOfHeader;
	}

	/*
     * 获取header的字符串数组，是构造burp中请求需要的格式。
     * 包含了协议行（第一行）
     */
	public List<String> getHeaderList(){
		List<String> HeaderList = new ArrayList<String>();
		for (Entry<String,String> header:this.headerMap.entrySet()) {
			String item = header.getKey()+Header_Spliter+header.getValue();
			HeaderList.add(item);
		}
		HeaderList.add(0,firstLineOfHeader);
		return HeaderList;
	}

	public IHttpRequestResponse getMessageInfo(){//重新组装数据包

		if (messageIsRequest){
			byte[] new_Request = helpers.buildHttpMessage(getHeaderList(),this.body);
			messageInfo.setRequest(new_Request);
			messageInfo.setHttpService(service);
		}else {
			byte[] new_Response = helpers.buildHttpMessage(getHeaderList(),this.body);
			messageInfo.setResponse(new_Response);
		}
		return messageInfo;
	}

	//////////*************常用修改 messageInfo的 函数*******************///////////////////

	public LinkedHashMap<String,String> getHeaderMap() {
		return this.headerMap;
	}

	public void setHeaderMap(LinkedHashMap<String, String> headerMap) {
		this.headerMap = headerMap;
	}

	public byte[] getBody() {
		return this.body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public IHttpService getService() {
		return service;
	}

	public void setService(IHttpService service) {
		this.service = service;
	}

	/*
	 * 获取所有headers，当做一个string看待。
	 * 主要用于判断是否包含某个特殊字符串
	 * List<String> getHeaders 调用toString()方法，得到如下格式：[111111, 2222]
	 * 就能满足上面的场景了
	 */
	public String getHeaderString() {
		List<String> headers =getHeaderList();
		StringBuilder headerString = new StringBuilder();

		for (String header : headers) {
			headerString.append(header);
		}

		return headerString.toString();
	}
	
	/*
	 * 获取header的map格式，key:value形式
	 * 这种方式可以用put函数轻松实现：如果有则update，如果无则add。
	 * 删除也很方便，直接remove，有则执行，无则跳过。
	 * 获取某个header值也方便。
	 * ！！！注意：这个方法获取到的map，会少了协议头GET /cps.gec/limit/information.html HTTP/1.1
	 */


	public String getShortUrl() {
		return messageInfo.getHttpService().toString();
	}
	
	public URL getURL(){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getUrl();
		
		//callbacks.getHelpers().analyzeRequest(baseRequestResponse).getUrl();
	}
	
	public String getHost() {
		return messageInfo.getHttpService().getHost();
	}
	
	public short getStatusCode() {
		IResponseInfo analyzedResponse = helpers.analyzeResponse(messageInfo.getResponse());
		return analyzedResponse.getStatusCode();
	}
	public List<IParameter> getParameters(){
		IRequestInfo analyzeRequest = helpers.analyzeRequest(messageInfo);
		return analyzeRequest.getParameters();
	}

	public String getMd5(){
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			byte[] bytes;
			if (messageIsRequest){
				bytes = this.getMessageInfo().getRequest();
			}else{
				bytes = this.getMessageInfo().getResponse();
			}
			m.update(bytes);
			byte s[] = m.digest();
			String result = "";
			for (int i = 0; i < s.length; i++) {
				result += Integer.toHexString((0x000000FF & s[i]) | 0xFFFFFF00).substring(6);
			}
			return result;
		}catch (Exception e){
			return null;
		}
	}
	//////////*************常用Get 函数*******************///////////////////

    
    public static void main(String args[]) {
		HashMap<String,String> xxx = new HashMap<String,String>();
		xxx.put("1","1");
		xxx.remove("2");

    	String a= "xxxxx%s%bxxxxxxx";
    	System.out.println(xxx);
    }
}
