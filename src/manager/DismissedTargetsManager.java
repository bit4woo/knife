package manager;

import java.util.ArrayList;
import java.util.List;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IInterceptedProxyMessage;
import config.ConfigEntry;
import config.GUI;

public class DismissedTargetsManager {

	private static final String Forward = "Forward";
	private static final String Drop = "Drop";

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
	 * @param messages
	 * @param action
	 */
	public static void putRule(IHttpRequestResponse[] messages,String keyword,String action ) {

		if (messages != null) {
			for(IHttpRequestResponse message:messages) {
				String host = getHost(message);
				String url = getUrl(message);

				if (action.equalsIgnoreCase(ConfigEntry.Action_Drop_Request_If_Host_Matches)
						|| action.equalsIgnoreCase(ConfigEntry.Action_Forward_Request_If_Host_Matches)) {

					delSameConditionRule(host);
					GUI.tableModel.addNewConfigEntry(new ConfigEntry(host, "",action,true));
				}

				if (action.equalsIgnoreCase(ConfigEntry.Action_Drop_Request_If_URL_Matches)
						|| action.equalsIgnoreCase(ConfigEntry.Action_Forward_Request_If_URL_Matches)) {

					delSameConditionRule(url);
					GUI.tableModel.addNewConfigEntry(new ConfigEntry(url, "",action,true));
				}
			}
		}

		if (keyword != null && !keyword.equals("")) {
			if (action.equalsIgnoreCase(ConfigEntry.Action_Drop_Request_If_Keyword_Matches)
					|| action.equalsIgnoreCase(ConfigEntry.Action_Forward_Request_If_Keyword_Matches)) {

				delSameConditionRule(keyword);
				GUI.tableModel.addNewConfigEntry(new ConfigEntry(keyword, "",action,true));
			}
		}
	}


	/**
	 * 判断是否存在相同条件的规则，如果存在应当删除旧的规则
	 * @param messages
	 * @param action
	 */
	public static void delSameConditionRule(String configKey) {
		List<ConfigEntry> rules = GetAllDropOrForwardRules();
		for (int i= rules.size()-1;i>=0;i--) {
			ConfigEntry rule = rules.get(i);
			if (rule.getKey().equals(configKey)) {
				GUI.tableModel.removeConfigEntry(rule);
			}
		}
	}

	/**
	 * 
	 * @param messages
	 */
	public static void removeRule(IHttpRequestResponse[] messages) {
		for(IHttpRequestResponse message:messages) {
			while (true) {
				//有可能多个规则都影响某个数据包
				List<ConfigEntry> rules = GetAllDropOrForwardRules();
				MatchResult res = whichAction(rules,message);
				if (res.getAction() == null || res.getAction().equals("")){
					break;//无规则命中
				}else {
					ConfigEntry Rule =res.getRule();
					if (Rule != null) {
						GUI.tableModel.removeConfigEntry(Rule);
					}
				}
			}
		}
	}


	/**
	 * 获取当前数据包应该执行的action
	 * 规则的匹配按照时间优先顺序进行：
	 * 越是新的规则，优先级越高。因为越是新的设置，越是能表示操作者当前的意愿。
	 * 
	 * 当要移除规则时，需要遍历所有drop和forward规则
	 */
	private static MatchResult whichAction(List<ConfigEntry> rules,IHttpRequestResponse message) {

		for (int index=rules.size()-1;index>=0;index--) {
			ConfigEntry rule = rules.get(index);
			MatchResult res = whichAction(rule,message);
			if (res.getAction() != null) {
				return res;
			}
		}
		return new MatchResult(null,null);
	}

	/**
	 * 
	 * @param message
	 * @return drop forward "" 空字符串表示什么也不做
	 */
	private static MatchResult whichAction(ConfigEntry rule,IHttpRequestResponse message) {
		String host = getHost(message);//域名不应该大小写敏感
		String url = getUrl(message);//URL中可能包含大写字母比如getUserInfo，URL应该是大小写敏感的。

		if (rule.getType().equals(ConfigEntry.Action_Forward_And_Hide_Options) && rule.isEnable()) {
			HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
			String method = getter.getMethod(message);
			if (method.equals("OPTIONS")) {
				return new MatchResult(Forward, rule);
			}
		}

		if (rule.getType().equalsIgnoreCase(ConfigEntry.Action_Drop_Request_If_Host_Matches)) {
			if (host.equalsIgnoreCase(rule.getKey())) {
				return new MatchResult(Drop, rule);
			}

			if (rule.getKey().startsWith("*.")) {
				String tmpDomain = rule.getKey().replaceFirst("\\*","");
				if (host.toLowerCase().endsWith(tmpDomain.toLowerCase())){
					return new MatchResult(Drop, rule);
				}
			}
		}
		if (rule.getType().equalsIgnoreCase(ConfigEntry.Action_Drop_Request_If_URL_Matches)) {
			if (url.equalsIgnoreCase(rule.getKey())) {
				return new MatchResult(Drop, rule);
			}
		}
		if (rule.getType().equalsIgnoreCase(ConfigEntry.Action_Drop_Request_If_Keyword_Matches)) {
			if (url.contains(rule.getKey())) {
				return new MatchResult(Drop, rule);
			}
		}
		if (rule.getType().equalsIgnoreCase(ConfigEntry.Action_Forward_Request_If_Host_Matches)) {
			if (host.equalsIgnoreCase(rule.getKey())) {
				return new MatchResult(Forward, rule);
			}

			if (rule.getKey().startsWith("*.")) {
				String tmpDomain = rule.getKey().replaceFirst("\\*","");
				if (host.toLowerCase().endsWith(tmpDomain.toLowerCase())){
					return new MatchResult(Forward, rule);
				}
			}
		}
		if (rule.getType().equalsIgnoreCase(ConfigEntry.Action_Forward_Request_If_URL_Matches)) {
			if (url.equalsIgnoreCase(rule.getKey())) {
				return new MatchResult(Forward, rule);
			}
		}
		if (rule.getType().equalsIgnoreCase(ConfigEntry.Action_Forward_Request_If_Keyword_Matches)) {
			if (url.contains(rule.getKey())) {
				return new MatchResult(Forward, rule);
			}
		}

		return new MatchResult(null,null);
	}

	public static List<ConfigEntry> GetAllDropOrForwardRules() {
		List<ConfigEntry> result = new ArrayList<ConfigEntry>();

		List<ConfigEntry> entries = GUI.tableModel.getConfigEntries();
		for (ConfigEntry entry:entries) {
			if (entry.isDropOrForwardActionType()) {
				if (entry.isEnable()) {
					result.add(entry);
				}
			}
		}
		return result;
	}

	/**
	 * 获取所有drop规则，可以先处理这些规则。
	 * @return
	 */
	public static List<ConfigEntry> getAllDropRules() {
		List<ConfigEntry> result = new ArrayList<ConfigEntry>();
		List<ConfigEntry> entries = GUI.tableModel.getConfigEntries();
		for (ConfigEntry entry:entries) {
			if (entry.isDropActionType()) {
				if (entry.isEnable()) {
					result.add(entry);
				}
			}
		}
		return result;
	}


	/**
	 * 获取所有对数据包进行修改的规则，除了drop规则。
	 * @return
	 */
	public static List<ConfigEntry> getAllChangeActionExceptDropRules() {
		List<ConfigEntry> result = new ArrayList<ConfigEntry>();
		List<ConfigEntry> entries = GUI.tableModel.getConfigEntries();
		for (ConfigEntry entry:entries) {
			if (entry.isActionType()) {
				if (!entry.isDropActionType()) {
					result.add(entry);
				}
			}
		}
		return result;
	}


	/**
	 * 返回这个数据是否被丢弃或者转发了。以便上层逻辑决定是否需要继续处理
	 * @param message
	 * @return is Dropped
	 */
	public static boolean checkDropAction(boolean messageIsRequest,IInterceptedProxyMessage message) {
		if (messageIsRequest) {
			List<ConfigEntry> rules = getAllDropRules();
			MatchResult res = whichAction(rules,message.getMessageInfo());
			//BurpExtender.getStdout().println(res.toString());//for debug
			if(res.getAction()== null) {
				return false;
			}
			if (res.getAction().equalsIgnoreCase(Drop)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DROP);
				message.getMessageInfo().setComment("Auto Dropped by Knife");
				message.getMessageInfo().setHighlight("gray");
				return true;
			}
		}
		return false;
	}

	/**
	 * 返回这个数据是否被转发了。以便上层逻辑决定是否需要继续处理
	 * @param message
	 * @return
	 */
	public static void checkForwardAction(ConfigEntry rule,boolean messageIsRequest,IInterceptedProxyMessage message) {
		if (messageIsRequest) {
			MatchResult res = whichAction(rule,message.getMessageInfo());
			//BurpExtender.getStdout().println(res.toString());//for debug
			if(res.getAction()== null) {
				return;
			}
			if (res.getAction().equalsIgnoreCase(Forward)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DONT_INTERCEPT);
				message.getMessageInfo().setComment("Auto Forwarded By Knife");
				//message.getMessageInfo().setHighlight("gray");
			}
		}
	}
}
