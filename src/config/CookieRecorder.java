package config;

import org.apache.commons.lang3.StringUtils;

/**
 * 根据当前请求信息，去查找可能的Cookie
 *
 */
public class CookieRecorder {
	String sameSiteCookie; //protocol+host+port都相同
	String sameRootDomainCookie; //rootDomain相同，比如统一登录的场景
	String sameOriginCookie;//好像还没有什么情况需要这种，后续再看
	String samePathCookie;//host不相同，但是URL Path相同
	

	public String getSameSiteCookie() {
		return sameSiteCookie;
	}
	public void setSameSiteCookie(String sameSiteCookie) {
		this.sameSiteCookie = sameSiteCookie;
	}
	public String getSameRootDomainCookie() {
		return sameRootDomainCookie;
	}
	public void setSameRootDomainCookie(String sameRootDomainCookie) {
		this.sameRootDomainCookie = sameRootDomainCookie;
	}
	public String getSamePathCookie() {
		return samePathCookie;
	}
	public void setSamePathCookie(String samePathCookie) {
		this.samePathCookie = samePathCookie;
	}
	
	public boolean isAllBlank() {
		return StringUtils.isAllBlank(sameSiteCookie,sameRootDomainCookie,samePathCookie);
	}
	
	public String getValue() {
		
		String result = sameSiteCookie;
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		
		result = sameRootDomainCookie;
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		
		result = samePathCookie;
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		return null;
	}
	
}
