package config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import burp.IBurpExtenderCallbacks;

public class ConfigManager {

	private String configManagerName = "";
	private List<String> stringConfigEntries = new ArrayList<>();// get from configTableModel
	private int enableStatus = IBurpExtenderCallbacks.TOOL_PROXY;
	private boolean onlyForScope = true;

	ConfigManager(){
		//to resolve "default constructor not found" error
	}

	public ConfigManager(String configManagerName){
		this.configManagerName = configManagerName;
	}

	public String getConfigManagerName() {
		return configManagerName;
	}

	public void setConfigManagerName(String configManagerName) {
		this.configManagerName = configManagerName;
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


	public String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().toJson(this);
	}

	public ConfigManager FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().fromJson(json, ConfigManager.class);
	}
}
