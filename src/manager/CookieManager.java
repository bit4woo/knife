package manager;

import java.util.LinkedHashMap;
import java.util.List;

import burp.BurpExtender;
import burp.Getter;
import burp.IHttpRequestResponse;
import burp.Methods;

public class CookieManager {

    //////////////////////////////////////////common methods for cookie handle///////////////////////////////

    public static String SPLITER = "::::";

    public static IHttpRequestResponse[] Reverse(IHttpRequestResponse[] input){
        for (int start = 0, end = input.length - 1; start < end; start++, end--) {
            IHttpRequestResponse temp = input[end];
            input[end] = input[start];
            input[start] = temp;
        }
        return input;
    }


	/*
	return a String url_which_cookie_from+SPLITER+cookievalue
	 */

    public static HeaderEntry getLatestHeaderFromHistory(String shortUrl,String headerName){
        //还是草粉师傅说得对，直接从history里面拿最好

    	shortUrl = Getter.formateURLString(shortUrl);//url格式标准化，以保证后面比较的准确性。
        IHttpRequestResponse[]  historyMessages = Reverse(BurpExtender.callbacks.getProxyHistory());
        Getter getter = new Getter(BurpExtender.callbacks.getHelpers());

        for (IHttpRequestResponse historyMessage:historyMessages) {
            String hisShortUrl = getter.getShortURL(historyMessage).toString();

            if (hisShortUrl.equalsIgnoreCase(shortUrl)) {
                String cookieValue = getter.getHeaderValueOf(true,historyMessage,headerName);
                if (cookieValue != null){
                	HeaderEntry entry = new HeaderEntry(shortUrl,headerName,cookieValue, null);
                	return entry;
                    //return shortUrl+SPLITER+cookieValue;
                }
            }
        }

        return null;
    }

    public static HeaderEntry getLatestCookieFromHistory(String shortUrl){
        return getLatestHeaderFromHistory(shortUrl,"Cookie");
    }
    
    
    //Cookie: ISIC_SOP_DES_S22_NG_WEB=ISIC_SOP_DES_S22_NG_196_8; a_authorization_sit=18ac8987-2059-4a3b-a433-7def12dbae4d/97cd8cce-20ba-40df-ac44-0adae67ae2ad/BF32FB9F1479F653496C56DC99299483; custom.name=f12c5888-467d-49af-bcab-9cf4a44c03ff
    //判断字符串是否是合格的cookie，每个分号分割的部分是否都是键值对格式。
    public static boolean isCookieString(String input) {
        String cookieValue = input.trim();

        if (cookieValue.startsWith("Cookie:")){
        	cookieValue = cookieValue.replaceFirst("Cookie:","").trim();
        }
        
        String[] items = input.split(";");
        for (String item: items) {
        	item = item.trim();
        	if (item.equals("")) {
        		continue;
        	}else if (!item.contains("=")) {
        		return false;
        	}
        }
        return true;
    }

    /*
    return a String url_which_cookie_from+SPLITER+cookievalue
     */
    public static HeaderEntry getLatestCookieFromSpeicified() {
        HeaderEntry latestCookie = null;
        String domainOrCookie = Methods.prompt_and_validate_input("cookie OR cookie of ", null);
        String url1 = "";
        String url2 = "";
        try{
            if (domainOrCookie == null){
                return null;
            }else if (isCookieString(domainOrCookie)){//直接是cookie
                String cookieValue = domainOrCookie.trim();

                if (cookieValue.startsWith("Cookie:")){
                	cookieValue = cookieValue.replaceFirst("Cookie:","").trim();
                }
                String tips = "Cookie: "+cookieValue.substring(0,cookieValue.indexOf("="))+"...";
                latestCookie = new HeaderEntry(tips,"Cookie",cookieValue, null);

                return latestCookie;
            }else if (domainOrCookie.startsWith("http://") || domainOrCookie.startsWith("https://")) {//不包含协议头的域名或url
                url1 = domainOrCookie;
            }else {
                url1 = "http://"+domainOrCookie;
                url2 = "https://"+domainOrCookie;
            }

            try {
            	url1 = Getter.formateURLString(url1);
            	url2 = Getter.formateURLString(url2);
                latestCookie = getLatestCookieFromHistory(url1);
                if (latestCookie == null && url2 != ""){
                    latestCookie = getLatestCookieFromHistory(url2);
                }
            } catch (Exception e) {
            	e.printStackTrace();
            }
            return latestCookie;

        }catch(NumberFormatException nfe){
            Methods.show_message("Enter proper domain!!!", "Input Not Valid");
        }
        return null;
    }

    public static byte[] updateCookie(IHttpRequestResponse messageInfo,String cookieValue){
        Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
        LinkedHashMap<String, String> headers = getter.getHeaderMap(true,messageInfo);
        byte[] body = getter.getBody(true,messageInfo);

        if(cookieValue.startsWith("Cookie: ")) {
            cookieValue = cookieValue.replaceFirst("Cookie: ","");
        }
        headers.put("Cookie",cookieValue);
        List<String> headerList = getter.headerMapToHeaderList(headers);

        byte[] newRequestBytes = BurpExtender.callbacks.getHelpers().buildHttpMessage(headerList, body);
        return newRequestBytes;
    }
}
