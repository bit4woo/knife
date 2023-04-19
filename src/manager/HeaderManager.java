package manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.ConfigEntry;
import config.GUI;

public class HeaderManager {

	//////////////////////////////////////////common methods for cookie handle///////////////////////////////

	//目标httpservice 和 headerLine
	private static String usedCookieOfUpdate = null;

	public static String getUsedCookieOfUpdate() {
		return usedCookieOfUpdate;
	}

	public static void setUsedCookieOfUpdate(String usedCookieOfUpdate) {
		HeaderManager.usedCookieOfUpdate = usedCookieOfUpdate;
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

	/**
	 * 是否有必要从sitemap中获取，如果它是按照时间排序的话，还是有用的。后续测试一下//TODO
	 * @param shortUrl
	 * @param headerName
	 * @return
	 */
	public static String getLatestHeaderFromSiteMap(String shortUrl,String headerName){
		//还是草粉师傅说得对，直接从history里面拿最好

		shortUrl = HelperPlus.removeDefaultPort(shortUrl);//url格式标准化，以保证后面比较的准确性。
		IHttpRequestResponse[]  historyMessages = BurpExtender.callbacks.getSiteMap(shortUrl);
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


	@Deprecated //没有应用场景
	public static IHttpRequestResponse checkURLBasedRuleAndTakeAction(List<ConfigEntry> rules,boolean messageIsRequest, IHttpRequestResponse messageInfo){

		for (int index=rules.size()-1;index>=0;index--) {
			ConfigEntry rule = rules.get(index);
			checkURLBasedRuleAndTakeAction(rule,messageIsRequest,messageInfo);
		}

		return messageInfo;
	}

	/**
	 * 处理 以base url作为判断依据的规则
	 * @param rules
	 * @param messageIsRequest
	 * @param messageInfo
	 * @return
	 */
	public static IHttpRequestResponse checkURLBasedRuleAndTakeAction(ConfigEntry rule,boolean messageIsRequest, IHttpRequestResponse messageInfo){

		byte[] oldRequest = messageInfo.getRequest();

		HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());

		String fullUrl = getter.getFullURL(messageInfo).toString();
		String targetShortUrl = HelperPlus.getShortURL(messageInfo).toString();
		String host = HelperPlus.getHost(messageInfo);


		String rulekey = rule.getKey();
		String rulevalue = rule.getValue();

		if (rulekey.equalsIgnoreCase(targetShortUrl) ||
				fullUrl.startsWith(rulekey)) {

			String headerName = rulevalue.split(":")[0].trim();
			String headerValue = rulevalue.split(":")[1].trim();

			if (headerValue.contains("%host")) {
				headerValue = headerValue.replaceAll("%host", host);
			}

			if (headerValue.toLowerCase().contains("%dnslogserver")) {
				String dnslog = GUI.tableModel.getConfigValueByKey("DNSlogServer");
				Pattern p = Pattern.compile("(?u)%dnslogserver");
				Matcher m = p.matcher(headerValue);

				while (m.find()) {
					String found = m.group(0);
					headerValue = headerValue.replaceAll(found, dnslog);
				}
			}

			if (rule.getType().equals(ConfigEntry.Action_If_Base_URL_Matches_Remove_From_Headers) && rule.isEnable()) {

				getter.removeHeader(messageIsRequest, messageInfo, headerName);
			}

			if (rule.getType().equals(ConfigEntry.Action_If_Base_URL_Matches_Add_Or_Replace_Header) && rule.isEnable()) {
				BurpExtender.getStdout().println(messageInfo.getHttpService().toString()+" message changed");
				getter.addOrUpdateHeader(messageIsRequest,messageInfo,headerName,headerValue);
			}

			if (rule.getType().equals(ConfigEntry.Action_If_Base_URL_Matches_Append_To_header_value) && rule.isEnable()) {
				String oldValue = getter.getHeaderValueOf(messageIsRequest, messageInfo, headerName);
				if (oldValue == null) {
					oldValue = "";
				}
				headerValue = oldValue + headerValue;
				getter.addOrUpdateHeader(messageIsRequest, messageInfo, headerName, headerValue);
			}
		}


		byte[] newRequest = messageInfo.getRequest();

		if (!Arrays.equals(newRequest,oldRequest)){
			//https://stackoverflow.com/questions/9499560/how-to-compare-the-java-byte-array
			messageInfo.setComment("auto changed by knife");
		}
		//BurpExtender.getStderr().println(messageInfo.getHttpService().toString()+" message not changed");
		return messageInfo;
	}


	/**
	 * 处理 以toolFlag 和Scope为判断依据的规则
	 * @param rules
	 * @param messageIsRequest
	 * @param messageInfo
	 * @return
	 */
	@Deprecated //没有应用场景
	public static IHttpRequestResponse checkScopeBasedRuleAndTakeAction(List<ConfigEntry> rules,boolean messageIsRequest, IHttpRequestResponse messageInfo){
		for (int index=rules.size()-1;index>=0;index--) {
			ConfigEntry rule = rules.get(index);
			checkScopeBasedRuleAndTakeAction(rule,messageIsRequest,messageInfo);
		}
		return messageInfo;
	}

	/**
	 * 处理 以toolFlag 和Scope为判断依据的规则。scope和toolflag的判断不在这个函数中
	 * @param rules
	 * @param messageIsRequest
	 * @param messageInfo
	 * @return
	 */
	public static IHttpRequestResponse checkScopeBasedRuleAndTakeAction(ConfigEntry rule,boolean messageIsRequest, IHttpRequestResponse messageInfo){

		byte[] oldRequest = messageInfo.getRequest();

		HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
		String host = HelperPlus.getHost(messageInfo);

		String key = rule.getKey();
		String value = rule.getValue();

		if (value.contains("%host")) {
			value = value.replaceAll("%host", host);
		}

		if (value.toLowerCase().contains("%dnslogserver")) {
			String dnslog = GUI.tableModel.getConfigValueByKey("DNSlogServer");
			Pattern p = Pattern.compile("(?u)%dnslogserver");
			Matcher m = p.matcher(value);

			while (m.find()) {
				String found = m.group(0);
				value = value.replaceAll(found, dnslog);
			}
		}


		//remove header
		if (rule.getType().equals(ConfigEntry.Action_Remove_From_Headers) && rule.isEnable()) {
			getter.removeHeader(messageIsRequest, messageInfo, key);
		}

		//add/update/append header
		if (rule.getType().equals(ConfigEntry.Action_Add_Or_Replace_Header) && rule.isEnable()) {
			getter.addOrUpdateHeader(messageIsRequest, messageInfo, key, value);

		}
		if (rule.getType().equals(ConfigEntry.Action_Append_To_header_value) && rule.isEnable()) {
			String oldValue = getter.getHeaderValueOf(messageIsRequest, messageInfo, key);
			if (oldValue == null) {
				oldValue = "";
			}
			value = oldValue + value;
			getter.addOrUpdateHeader(messageIsRequest, messageInfo, key, value);
		} 


		byte[] newRequest = messageInfo.getRequest();

		if (!Arrays.equals(newRequest,oldRequest)){
			//https://stackoverflow.com/questions/9499560/how-to-compare-the-java-byte-array
			messageInfo.setComment("auto changed by knife");
		}
		//BurpExtender.getStderr().println(messageInfo.getHttpService().toString()+" message not changed");
		return messageInfo;
	}


	public static IHttpRequestResponse updateCookie(boolean messageIsRequest, IHttpRequestResponse messageInfo, String cookieValue){
		return updateHeader(messageIsRequest,messageInfo,cookieValue);
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
		for(IHttpRequestResponse message:messages) {
			String targetShortUrl = HelperPlus.getShortURL(message).toString();
			ConfigEntry rule = new ConfigEntry(targetShortUrl,headerLine,ConfigEntry.Action_If_Base_URL_Matches_Add_Or_Replace_Header,true);
			GUI.tableModel.delSameRule(rule);
			GUI.tableModel.addNewConfigEntry(rule);
			BurpExtender.getStdout().println("new handle rule added: "+targetShortUrl+" : "+headerLine);
		}
	}

	public static List<ConfigEntry> GetHeaderHandleWithIfRules() {
		List<ConfigEntry> result = new ArrayList<ConfigEntry>();

		List<ConfigEntry> entries = GUI.tableModel.getConfigEntries();
		for (ConfigEntry entry:entries) {
			if (entry.isHeaderHandleWithIfActionType()) {
				if (entry.isEnable()) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 * 获取所有对数据包进行修改的规则，除了drop和forward规则。
	 * @return
	 */
	public static List<ConfigEntry> getAllChangeRules() {
		List<ConfigEntry> result = new ArrayList<ConfigEntry>();
		List<ConfigEntry> entries = GUI.tableModel.getConfigEntries();
		for (ConfigEntry entry:entries) {
			if (entry.isActionType()) {
				if (!entry.isDropOrForwardActionType()) {
					result.add(entry);
				}
			}
		}
		return result;
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
