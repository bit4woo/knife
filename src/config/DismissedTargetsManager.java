package config;

import java.util.HashMap;

import com.google.gson.Gson;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IInterceptedProxyMessage;

public class DismissedTargetsManager {
	private static final String Forward = "Forward";
	private static final String Drop = "Drop";

	public static final String ACTION_Forward_URL = "Forward_URL";
	public static final String ACTION_Forward_HOST = "Forward_HOST";
	public static final String ACTION_DROP_URL = "Drop_URL";
	public static final String ACTION_DROP_HOST = "Drop_HOST";

	private static HashMap<String, String> rules = new HashMap<String,String>();


	public static String getHost(IHttpRequestResponse message) {//如果存在host参数，会被用于序列化，注意
		String host = message.getHttpService().getHost();
		return host;
	}

	public static String getUrl(IHttpRequestResponse message) {//如果存在host参数，会被用于序列化，注意
		IExtensionHelpers helpers = BurpExtender.getCallbacks().getHelpers();
		String url = new HelperPlus(helpers).getFullURL(message).toString();
		if (url.contains("?")){
			url = url.substring(0,url.indexOf("?"));
		}
		return url;
	}

	/**
	 * 修改规则前后，都应该和GUI同步
	 * @param message
	 * @param action
	 */
	public static void putRule(IHttpRequestResponse[] messages,String action ) {
		fetchRulesFromGUI();
		for(IHttpRequestResponse message:messages) {
			String host = getHost(message);
			String url = getUrl(message);

			if (action.equalsIgnoreCase(ACTION_Forward_URL)) {
				rules.put(url, Forward);
			}else if(action.equalsIgnoreCase(ACTION_Forward_HOST)) {
				rules.put(host, Forward);
			}else if(action.equalsIgnoreCase(ACTION_DROP_URL)) {
				rules.put(url, Drop);
			}else if(action.equalsIgnoreCase(ACTION_DROP_HOST)) {
				rules.put(host, Drop);
			}
		}
		ShowRulesToGUI();
	}

	/**
	 * todo
	 * @param message
	 * @param action
	 */
	public static void removeRule(IHttpRequestResponse[] messages) {
		fetchRulesFromGUI();
		for(IHttpRequestResponse message:messages) {
			String host = getHost(message);
			String url = getUrl(message);

			rules.remove(url);
			rules.remove(host);
		}

		ShowRulesToGUI();
	}

	/**
	 * 获取当前数据包应该执行的action
	 * @param message
	 * @return
	 */
	private static String whichAction(IHttpRequestResponse message) {

		String host = getHost(message);
		String url = getUrl(message);

		fetchRulesFromGUI();

		for (String key:rules.keySet()) {
			key = key.toLowerCase();
			if (url.startsWith(key)) {//先匹配host规则
				return rules.get(key);
			}
			if (host.equalsIgnoreCase(key)) {//再匹配URL规则
				return rules.get(key);
			}

			/*
			Pattern pt = Pattern.compile(key);
			Matcher matcher = pt.matcher(url);
			if (matcher.find()) {//多次查找
				return targets.get(key);
			}*/

			if (key.startsWith("*.")){
				String tmpDomain = key.replaceFirst("\\*","");
				if (host.endsWith(tmpDomain)){
					return rules.get(key);
				}
			}
		}
		return "";
	}

	/**
	 * 返回这个数据是否被丢弃或者转发了。以便上层逻辑决定是否需要继续处理
	 * @param message
	 * @return
	 */
	public static boolean checkAndDoAction(boolean messageIsRequest,IInterceptedProxyMessage message) {
		if (messageIsRequest) {
			String action = whichAction(message.getMessageInfo());

			if (action.equalsIgnoreCase(Forward)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DONT_INTERCEPT);
				message.getMessageInfo().setComment("Auto Forwarded By Knife");
				message.getMessageInfo().setHighlight("gray");
				return true;
			}
			if (action.equalsIgnoreCase(Drop)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DROP);
				message.getMessageInfo().setComment("Auto Dropped by Knife");
				message.getMessageInfo().setHighlight("gray");
				return true;
			}
		}
		return false;

	}

	/**
	 * 将Map转换为Json格式的字符串
	 * @return
	 */
	public static String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().toJson(rules);
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
	public static void fetchRulesFromGUI() {
		String dissmissed  = GUI.tableModel.getConfigValueByKey("DismissedTargets");
		try {
			rules = FromJson(dissmissed);
		}catch (Exception e) {
			targetsInit();
		}
		if (rules == null) {
			targetsInit();
		}
	}

	public static void targetsInit() {
		rules = new HashMap<String,String>();
		rules.put("*.firefox.com", Drop);
		rules.put("*.mozilla.com", Drop);
	}

	/**
	 * 将当前配置显示到GUI
	 */
	public static void ShowRulesToGUI() {
		GUI.tableModel.setConfigByKey("DismissedTargets",ToJson());
	}

}
