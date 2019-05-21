package knife;

import burp.*;

import java.util.LinkedHashMap;
import java.util.List;

public class CookieUtils {

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

    public static String getLatestHeaderFromHistory(String shortUrl,String headerName){
        //还是草粉师傅说得对，直接从history里面拿最好

        IHttpRequestResponse[]  historyMessages = Reverse(BurpExtender.callbacks.getProxyHistory());
        Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
        //callbacks.printOutput("length of history: "+ historyMessages.length);

/*        for (IHttpRequestResponse historyMessage:historyMessages) {
            //String hisShortUrl = historyMessage.getHttpService().toString();
            String hisUrlString = getter.getURL(historyMessage).toString().replaceFirst(":80/","/").replaceFirst(":443/","/");
            //这里转换成字符串会包含默认端口！！
            if (hisUrlString.startsWith(shortUrl)) {
                String cookieValue = getter.getHeaderValueOf(true,historyMessage,headerName);
                if (cookieValue != null){
                    return shortUrl+SPLITER+cookieValue;
                }
            }
        }*/

        //以上是完整URL精确匹配，如果失败，尝试根据短url粗糙匹配
        if (shortUrl.lastIndexOf("/") > "https://".length()){
            shortUrl = shortUrl.substring(0,shortUrl.indexOf("/",8));
        }
        for (IHttpRequestResponse historyMessage:historyMessages) {
            String hisShortUrl = historyMessage.getHttpService().toString();
            if (hisShortUrl.equalsIgnoreCase(shortUrl)) {
                String cookieValue = getter.getHeaderValueOf(true,historyMessage,headerName);
                if (cookieValue != null){
                    return shortUrl+SPLITER+cookieValue;
                }
            }
        }

        return null;
    }

    public static String getLatestCookieFromHistory(String shortUrl){
        return getLatestHeaderFromHistory(shortUrl,"Cookie");
    }

    /*
    return a String url_which_cookie_from+SPLITER+cookievalue
     */
    public static String getLatestCookieFromSpeicified() {
        String latestCookie = null;
        String domainOrCookie = Methods.prompt_and_validate_input("cookie OR cookie of ", null);
        String url1 = "";
        String url2 = "";
        try{
            if (domainOrCookie == null){
                return null;
            }else if (domainOrCookie.contains("=") && !domainOrCookie.contains("?") && !domainOrCookie.contains("/")){//直接是cookie
                latestCookie = domainOrCookie.trim();
                if (latestCookie.startsWith("Cookie:")){
                    latestCookie = latestCookie.replaceFirst("Cookie:","").trim();
                }
                String tips = "Cookie: "+latestCookie.substring(0,latestCookie.indexOf("="))+"...";
                latestCookie = tips+SPLITER+latestCookie;
                return latestCookie;
            }else if (domainOrCookie.startsWith("http://") || domainOrCookie.startsWith("https://")) {//不包含协议头的域名或url
                url1 = domainOrCookie;
            }else {
                url1 = "http://"+domainOrCookie;
                url2 = "https://"+domainOrCookie;
            }

            try {
                latestCookie = getLatestCookieFromHistory(url1);
                if (latestCookie == null && url2 != ""){
                    latestCookie = getLatestCookieFromHistory(url2);
                }
            } catch (Exception e) {

            }
            return latestCookie;

        }catch(NumberFormatException nfe){
            Methods.show_message("Enter proper domain!!!", "Input Not Valid");
        }
        return null;
    }

    public static byte[] updateCookie(IHttpRequestResponse messageInfo,String cookieValue){
        Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
        String firstline = getter.getHeaderFirstLine(true,messageInfo);
        LinkedHashMap<String, String> headers = getter.getHeaderHashMap(true,messageInfo);
        byte[] body = getter.getBody(true,messageInfo);

        if(cookieValue.startsWith("Cookie: ")) {
            cookieValue = cookieValue.replaceFirst("Cookie: ","");
        }
        headers.put("Cookie",cookieValue);
        List<String> headerList = getter.HeaderMapToList(firstline,headers);

        byte[] newRequestBytes = BurpExtender.callbacks.getHelpers().buildHttpMessage(headerList, body);
        return newRequestBytes;
    }
}
