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
        //callbacks.printOutput("length of history: "+ historyMessages.length);
        String lastestCookie =null;
        for (IHttpRequestResponse historyMessage:historyMessages) {
            String hisShortUrl = historyMessage.getHttpService().toString();
            if (hisShortUrl.equals(shortUrl)) {
                Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
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
        String domain = Methods.prompt_and_validate_input("update cookie with cookie of ", null);
        String url1 = "";
        String url2 = "";
        try{
            if (domain!= null && (domain.startsWith("http://") || domain.startsWith("https://"))) {
                url1 = domain;
            }else {
                url1 = "http://"+domain;
                url2 = "https://"+domain;
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
