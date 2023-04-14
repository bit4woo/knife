package config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;

import burp.IBurpExtenderCallbacks;
import manager.HeaderEntry;

public class Config {
	
	private String ConfigName = "";
	private List<String> stringConfigEntries = new ArrayList<String>();// get from configTableModel
	private int enableStatus = IBurpExtenderCallbacks.TOOL_PROXY;
	private boolean onlyForScope = true;
	private transient HashMap<String,HeaderEntry> setCookieMap = new HashMap<String,HeaderEntry>();
	private transient HeaderEntry usedCookie = null;
    
	Config(){
    	//to resolve "default constructor not found" error
	}
    
	public Config(String ConfigName){
		this.ConfigName = ConfigName;
	}

	public String getConfigName() {
		return ConfigName;
	}

	public void setConfigName(String configName) {
		ConfigName = configName;
	}

	public List<String> getStringConfigEntries() {
		return stringConfigEntries;
	}

	public void setStringConfigEntries(List<String> stringConfigEntries) {
		this.stringConfigEntries = stringConfigEntries;
	}

	public int getEnableStatus() {
		return enableStatus;
	}

	public void setEnableStatus(int enableStatus) {
		this.enableStatus = enableStatus;
	}

	public boolean isOnlyForScope() {
		return onlyForScope;
	}

	public void setOnlyForScope(boolean onlyForScope) {
		this.onlyForScope = onlyForScope;
	}


	public HashMap<String, HeaderEntry> getSetCookieMap() {
		return setCookieMap;
	}

	public void setSetCookieMap(HashMap<String, HeaderEntry> setCookieMap) {
		this.setCookieMap = setCookieMap;
	}

	public HeaderEntry getUsedCookie() {
		return usedCookie;
	}

	public void setUsedCookie(HeaderEntry usedCookie) {
		this.usedCookie = usedCookie;
	}

	public String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().toJson(this);
	}
	
	public Config FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().fromJson(json, Config.class);
	}
}
