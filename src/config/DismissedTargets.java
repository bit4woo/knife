package config;

import java.net.URL;
import java.util.HashMap;

import com.google.gson.Gson;

public class DismissedTargets {
	public static final String ACTION_DONT_INTERCEPT = "Forward";
	public static final String ACTION_DROP = "Drop";

	public static HashMap<String, String> targets = new HashMap<String,String>();

	public static String whichAction(String url) {

		String host = "";
		try {
			host = new URL(url).getHost().toLowerCase();
			if (url.contains("?")){
				url = url.substring(0,url.indexOf("?"));
			}
		}catch (Exception e) {
			return "";
		}

		FromGUI();
		for (String key:targets.keySet()) {
			key = key.toLowerCase();
			if (url.startsWith(key)) {
				return targets.get(key);
			}
			if (host.equalsIgnoreCase(key)) {
				return targets.get(key);
			}
			
			/*
			Pattern pt = Pattern.compile(key);
			Matcher matcher = pt.matcher(url);
			if (matcher.find()) {//多次查找
				return targets.get(key);
			}*/
			
			if (key.startsWith("*.")){
				key = key.replaceFirst("\\*","");
				if (host.endsWith(key)){
					return targets.get(key);
				}
			}
		}
		return "";
	}
	/**
	 * 将Map转换为Json格式的字符串
	 * @return
	 */
	public static String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().toJson(targets);
	}
	
	/**
	 * 将Json字符串转换为Map
	 * @param json
	 * @return
	 */
	public static HashMap<String,String> FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().fromJson(json, HashMap.class);
	}
	
	/**
	 * 从GUI读取配置，写入当前对象
	 */
	public static void FromGUI() {
		String dissmissed  = GUI.tableModel.getConfigValueByKey("DismissedTargets");
		try {
			targets = DismissedTargets.FromJson(dissmissed);
		}catch (Exception e) {
			targetsInit();
		}
		if (targets == null) {
			targetsInit();
		}
	}
	
	public static void targetsInit() {
		targets = new HashMap<String,String>();
		targets.put("*.firefox.com", ACTION_DROP);
		targets.put("*.mozilla.com", ACTION_DROP);
	}
	
	/**
	 * 将当前配置显示到GUI
	 */
	public static void ShowToGUI() {
		GUI.tableModel.setConfigByKey("DismissedTargets",ToJson());
	}
	
}
