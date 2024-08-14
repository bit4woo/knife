package config;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.DomainUtils;
import com.bit4woo.utilbox.utils.UrlUtils;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;

public class CookieFinder {

    
    public static CookieRecorder getLatestCookieFromHistory(IHttpRequestResponse messageInfo) {
        return getLatestHeaderFromHistory(messageInfo,"Cookie");
    }
    
    
    public static CookieRecorder getLatestCookieFromHistory(String sourceUrl) {
    	return getLatestHeader(sourceUrl,null,BurpExtender.callbacks.getProxyHistory(),"Cookie");
    }

    public static CookieRecorder getLatestHeaderFromHistory(IHttpRequestResponse messageInfo,String headerName) {
    	String sourceUrl = BurpExtender.getHelperPlus().getFullURL(messageInfo).toString();
        String originUrl = BurpExtender.getHelperPlus().getHeaderValueOf(true,messageInfo,"Origin");
        
        return getLatestHeader(sourceUrl,originUrl,BurpExtender.callbacks.getProxyHistory(),headerName);
    }
    
    /**
     * 
     * @param shortUrl
     * @param originUrl
     * @param historyMessages
     * @param headerName
     * @return
     */
    public static CookieRecorder getLatestHeader(String sourceUrl,String originUrl, IHttpRequestResponse[] historyMessages, String headerName) {
    	CookieRecorder result = new CookieRecorder();
    	
    	String shortUrl = UrlUtils.getBaseUrl(sourceUrl);//url格式标准化，以保证后面比较的准确性。
        shortUrl = HelperPlus.removeUrlDefaultPort(shortUrl);//url格式标准化，以保证后面比较的准确性。
        //String path = UrlUtils.getPath(sourceUrl);
        String host = UrlUtils.getHost(shortUrl);
        String rootDomain = null;
        String originHost = null;
        String originRootDomain = null;

        
    	if (DomainUtils.isValidDomainMayPort(host)) {
    		rootDomain = DomainUtils.getRootDomain(host);
    	}
    	
        if (StringUtils.isNoneBlank(originUrl)) {
        	originHost = UrlUtils.getHost(originUrl);
        	
        	if (DomainUtils.isValidDomainMayPort(originHost)) {
        		originRootDomain = DomainUtils.getRootDomain(originHost);
        	}
        }
        
        HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());

        for (int i = historyMessages.length - 1; i >= 0; i--) {
            IHttpRequestResponse historyMessage = historyMessages[i];
            String hisShortUrl = HelperPlus.getBaseURL(historyMessage).toString();
            hisShortUrl = HelperPlus.removeUrlDefaultPort(hisShortUrl);
            
            String hisHost = UrlUtils.getHost(shortUrl);
            String hisRootDomain = null;
        	if (DomainUtils.isValidDomainMayPort(hisHost)) {
        		hisRootDomain = DomainUtils.getRootDomain(hisHost);
        	}

        	//baseUrl完全相同
            if (hisShortUrl.equalsIgnoreCase(shortUrl)) {
                String headerLine = getter.getHeaderLine(true, historyMessage, headerName);
                if (StringUtils.isNotBlank(headerLine)){
                	result.setSameSiteCookie(headerLine);
                }
            }else {
            	//host是domain
            	if (StringUtils.isNotBlank(rootDomain)) {
            		if (StringUtils.isNotBlank(hisRootDomain) && rootDomain.equalsIgnoreCase(hisRootDomain)) {
            		      String headerLine = getter.getHeaderLine(true, historyMessage, headerName);
                          if (StringUtils.isNotBlank(headerLine)){
                          	result.setSameRootDomainCookie(headerLine);
                          }
            		}
            	}else {
            		//如果host是IP，可能出现IP和域名是同一个站点的情况。可以尝试查询相同路径的Cookie
            		
            	}
            }
        }
        return result;
    }
    


    /**
     * 是否有必要从sitemap中获取，如果它是按照时间排序的话，还是有用的。后续测试一下//TODO
     *
     * @param shortUrl
     * @param headerName
     * @return
     */
    public static CookieRecorder getLatestHeaderFromSiteMap(IHttpRequestResponse messageInfo) {
        return getLatestHeaderFromSiteMap(messageInfo,"Cookie");
    }

    public static CookieRecorder getLatestHeaderFromSiteMap(IHttpRequestResponse messageInfo,String headerName) {
    	String sourceUrl = BurpExtender.getHelperPlus().getFullURL(messageInfo).toString();
    	String shortUrl = HelperPlus.getBaseURL(messageInfo).toString();
        String originUrl = BurpExtender.getHelperPlus().getHeaderValueOf(true,messageInfo,"Origin");
        
        //这里是否使用shortUrl，对后续有明显的影响，详细考虑逻辑TODO
        return getLatestHeader(sourceUrl,originUrl,BurpExtender.callbacks.getSiteMap(shortUrl),headerName);
    }
    

    //Cookie: ISIC_SOP_DES_S22_NG_WEB=ISIC_SOP_DES_S22_NG_196_8; a_authorization_sit=18ac8987-2059-4a3b-a433-7def12dbae4d/97cd8cce-20ba-40df-ac44-0adae67ae2ad/BF32FB9F1479F653496C56DC99299483; custom.name=f12c5888-467d-49af-bcab-9cf4a44c03ff
    //判断字符串是否是合格的cookie，每个分号分割的部分是否都是键值对格式。
    public static boolean isValidCookieString(String input) {
        String cookieValue = input.trim();

        if (cookieValue.startsWith("Cookie:")) {
            cookieValue = cookieValue.replaceFirst("Cookie:", "").trim();
        }

        String[] items = cookieValue.split(";");
        for (String item : items) {
            if (!item.contains("=")) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回的内容格式是一个header line
     *
     * @return
     */
    public static String getLatestCookieFromUserInput() {
        String domainOrCookie = Methods.prompt_and_validate_input("Cookie Value OR URL That Cookie From ", null);
        String url1 = "";
        String url2 = "";
        try {
            if (domainOrCookie == null) {
                return null;
            } else if (isValidCookieString(domainOrCookie)) {//直接是cookie
                String cookieValue = domainOrCookie.trim();

                if (!cookieValue.startsWith("Cookie:")) {
                    cookieValue = "Cookie: " + cookieValue;
                }
                return cookieValue;
            } else if (domainOrCookie.startsWith("http://") || domainOrCookie.startsWith("https://")) {//不包含协议头的域名或url
                url1 = domainOrCookie;
            } else {
                url1 = "http://" + domainOrCookie;
                url2 = "https://" + domainOrCookie;
            }

            try {
            	CookieRecorder recorder = getLatestCookieFromHistory(url1);
                if (recorder.isAllBlank() && StringUtils.isNotBlank(url2)) {
                	recorder = getLatestCookieFromHistory(url2);
                }
                return recorder.getValue();
            } catch (Exception e) {
                e.printStackTrace(BurpExtender.getStderr());
            }
        } catch (Exception e) {
            e.printStackTrace(BurpExtender.getStderr());
            Methods.show_message("Enter proper domain!!!", "Input Not Valid");
        }
        return null;
    }

}
