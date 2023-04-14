package manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import burp.*;

public class CookieManager {

	//////////////////////////////////////////common methods for cookie handle///////////////////////////////

	private static HashMap<String, String> handleRules = new HashMap<String,String>();
	//目标httpservice 和 headerLine
	private static String usedCookieOfUpdate = null;

	public static String getUsedCookieOfUpdate() {
		return usedCookieOfUpdate;
	}

	public static void setUsedCookieOfUpdate(String usedCookieOfUpdate) {
		CookieManager.usedCookieOfUpdate = usedCookieOfUpdate;
	}


	public static String fetchUsedCookieAsTips() {
		String cookieValue = usedCookieOfUpdate.split(":")[1].trim();
		if (cookieValue.length()<=20){
			return cookieValue;
		}else {
			return cookieValue.substring(0,20)+"...";
		}
	}

	@Deprecated
	public static IHttpRequestResponse[] Reverse(IHttpRequestResponse[] input){
		for (int start = 0, end = input.length - 1; start < end; start++, end--) {
			IHttpRequestResponse temp = input[end];
			input[end] = input[start];
			input[start] = temp;
		}
		return input;
	}

	public static String getLatestHeaderFromHistory(IHttpRequestResponse messageInfo,String headerName){
		String sourceshorturl = HelperPlus.getShortURL(messageInfo).toString();
		return getLatestHeaderFromHistory(sourceshorturl,headerName);
	}

	public static String getLatestHeaderFromHistory(String shortUrl,String headerName){
		//还是草粉师傅说得对，直接从history里面拿最好

		shortUrl = HelperPlus.removeDefaultPort(shortUrl);//url格式标准化，以保证后面比较的准确性。
		IHttpRequestResponse[]  historyMessages = BurpExtender.callbacks.getProxyHistory();
		HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());

		for (int i=historyMessages.length-1; i>=0; i--) {
			IHttpRequestResponse historyMessage = historyMessages[i];
			String hisShortUrl = HelperPlus.getShortURL(historyMessage).toString();
			HelperPlus.removeDefaultPort(hisShortUrl);
			if (hisShortUrl.equalsIgnoreCase(shortUrl)) {
				String headerLine = getter.getHeaderLine(true,historyMessage,headerName);
				return headerLine;
			}
		}
		return null;
	}

	public static String getLatestCookieFromHistory(IHttpRequestResponse messageInfo){
		return getLatestHeaderFromHistory(messageInfo,"Cookie");
	}

	public static String getLatestCookieFromHistory(String shortUrl){
		return getLatestHeaderFromHistory(shortUrl,"Cookie");
	}


	//Cookie: ISIC_SOP_DES_S22_NG_WEB=ISIC_SOP_DES_S22_NG_196_8; a_authorization_sit=18ac8987-2059-4a3b-a433-7def12dbae4d/97cd8cce-20ba-40df-ac44-0adae67ae2ad/BF32FB9F1479F653496C56DC99299483; custom.name=f12c5888-467d-49af-bcab-9cf4a44c03ff
	//判断字符串是否是合格的cookie，每个分号分割的部分是否都是键值对格式。
	public static boolean isValidCookieString(String input) {
		String cookieValue = input.trim();

		if (cookieValue.startsWith("Cookie:")){
			cookieValue = cookieValue.replaceFirst("Cookie:","").trim();
		}

		String[] items = cookieValue.split(";");
		for (String item: items) {
			if (!item.contains("=")) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 返回的内容格式是一个header line
	 * @return
	 */
	public static String getLatestCookieFromUserInput() {
		String domainOrCookie = Methods.prompt_and_validate_input("Cookie Value OR URL That Cookie From ", null);
		String url1 = "";
		String url2 = "";
		try{
			if (domainOrCookie == null){
				return null;
			}else if (isValidCookieString(domainOrCookie)){//直接是cookie
				String cookieValue = domainOrCookie.trim();

				if (!cookieValue.startsWith("Cookie:")){
					cookieValue = "Cookie: "+cookieValue;
				}
				return cookieValue;
			}else if (domainOrCookie.startsWith("http://") || domainOrCookie.startsWith("https://")) {//不包含协议头的域名或url
				url1 = domainOrCookie;
			}else {
				url1 = "http://"+domainOrCookie;
				url2 = "https://"+domainOrCookie;
			}

			try {
				String latestCookie = getLatestCookieFromHistory(url1);
				if (latestCookie == null && !url2.equals("")){
					latestCookie = getLatestCookieFromHistory(url2);
				}
				return latestCookie;
			} catch (Exception e) {
				e.printStackTrace(BurpExtender.getStderr());
			}
		}catch(Exception e){
			e.printStackTrace(BurpExtender.getStderr());
			Methods.show_message("Enter proper domain!!!", "Input Not Valid");
		}
		return null;
	}

	public static IHttpRequestResponse checkHandleRuleAndTakeAction(boolean messageIsRequest, IHttpRequestResponse messageInfo){
		String targetShortUrl = HelperPlus.getShortURL(messageInfo).toString();
		if (targetShortUrl != null){
			String headerLine = handleRules.get(targetShortUrl);
			if (headerLine!=null){
				HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
				String headerName = headerLine.split(":")[0].trim();
				String headerValue = headerLine.split(":")[1].trim();
				return getter.addOrUpdateHeader(messageIsRequest,messageInfo,headerName,headerValue);
			}
		}
		BurpExtender.getStderr().println("message not changed");
		return messageInfo;
	}

	public static IHttpRequestResponse updateCookie(boolean messageIsRequest, IHttpRequestResponse messageInfo, String cookieValue){
		HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
		return getter.addOrUpdateHeader(messageIsRequest,messageInfo,"Cookie",cookieValue);
	}


	public static IHttpRequestResponse updateHeader(boolean messageIsRequest, IHttpRequestResponse messageInfo, String headerLine){
		try {
			HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
			String headerName = headerLine.split(":")[0].trim();
			String headerValue = headerLine.split(":")[1].trim();
			return getter.addOrUpdateHeader(messageIsRequest,messageInfo,headerName,headerValue);
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
			return messageInfo;
		}
	}

	public static void addHandleRule(IHttpRequestResponse[] messages,String headerLine) {
		IExtensionHelpers helpers = BurpExtender.callbacks.getHelpers();
		HelperPlus getter = new HelperPlus(helpers);
		for(IHttpRequestResponse message:messages) {
			String targetShortUrl = HelperPlus.getShortURL(message).toString();
			handleRules.put(targetShortUrl, headerLine);
		}
	}

	/**
	 * 生成响应包的”Set-Cookie: xxx“
	 * @param cookies
	 * @return
	 */
	public List<String> GenSetCookieHeaders(String cookies){
		if (cookies.startsWith("Cookie: ")){
			cookies = cookies.replaceFirst("Cookie: ","");
		}

		String[] cookieList = cookies.split("; ");
		List<String> setHeaderList= new ArrayList<String>();
		//Set-Cookie: SST_S22__WEB_RIGHTS=SST_S22_JT_RIGHTS_113_9; Path=/
		for (String cookie: cookieList){
			setHeaderList.add(String.format("Set-Cookie: %s; Path=/",cookie));
		}
		return setHeaderList;
	}
}
