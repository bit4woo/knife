package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.alibaba.fastjson.JSON;

import U2C.JSONBeautifier;
import U2C.U2CTab;
import config.Config;
import config.ConfigEntry;
import config.ConfigTable;
import config.ConfigTableModel;
import config.GUI;
import hackbar.File_Payload_Menu;
import hackbar.LFI_Menu;
import hackbar.Reverse_Shell_Menu;
import hackbar.SQL_Error;
import hackbar.SQL_Menu;
import hackbar.SQli_LoginBypass;
import hackbar.SSTI_Menu;
import hackbar.WebShell_Menu;
import hackbar.XSS_Menu;
import hackbar.XXE_Menu;
import knife.AddHostToScopeMenu;
import knife.ChunkedEncodingMenu;
import knife.CookieUtils;
import knife.DismissMenu;
import knife.DoActiveScanMenu;
import knife.DoPortScanMenu;
import knife.HeaderEntry;
import knife.InsertXSSMenu;
import knife.OpenWithBrowserMenu;
import knife.RunSQLMapMenu;
import knife.SetCookieMenu;
import knife.SetCookieWithHistoryMenu;
import knife.UpdateCookieMenu;
import knife.UpdateCookieWithHistoryMenu;
import knife.UpdateHeaderMenu;

public class BurpExtender extends GUI implements IBurpExtender, IContextMenuFactory, ITab, IHttpListener,IProxyListener,IExtensionStateListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
	public static PrintWriter stdout;
	public static PrintWriter stderr;
	public IContextMenuInvocation context;
	public int proxyServerIndex=-1;
	public static JSONBeautifier jsonBeautifier;
	public static U2CTab u2ctab;


	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		flushStd();
		BurpExtender.stdout.println(ExtensionName);
		BurpExtender.stdout.println(github);

		table = new ConfigTable(new ConfigTableModel());
		configPanel.setViewportView(table);

		String content = callbacks.loadExtensionSetting("knifeconfig");
		if (content!=null) {
			config = JSON.parseObject(content, Config.class);
			showToUI(config);
		}else {
			showToUI(JSON.parseObject(initConfig(), Config.class));
		}
		table.setupTypeColumn();//call this function must after table data loaded !!!!


		jsonBeautifier = new JSONBeautifier(null, false, helpers, callbacks);
		u2ctab = new U2CTab(null, false, helpers, callbacks);

		//各项数据初始化完成后在进行这些注册操作，避免插件加载时的空指针异常
		callbacks.setExtensionName(this.ExtensionName);
		callbacks.registerContextMenuFactory(this);// for menus
		callbacks.registerMessageEditorTabFactory(u2ctab);// for U2C
		callbacks.registerMessageEditorTabFactory(jsonBeautifier);
		callbacks.addSuiteTab(BurpExtender.this);
		callbacks.registerHttpListener(this);
		callbacks.registerProxyListener(this);
		callbacks.registerExtensionStateListener(this);
	}


	private static void flushStd(){
		try{
			stdout = new PrintWriter(callbacks.getStdout(), true);
			stderr = new PrintWriter(callbacks.getStderr(), true);
		}catch (Exception e){
			stdout = new PrintWriter(System.out, true);
			stderr = new PrintWriter(System.out, true);
		}
	}

	public static PrintWriter getStdout() {
		flushStd();//不同的时候调用这个参数，可能得到不同的值
		return stdout;
	}

	public static PrintWriter getStderr() {
		flushStd();
		return stderr;
	}

	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		this.context = invocation;

		ArrayList<JMenuItem> menu_list = new ArrayList<JMenuItem>();


		byte context = invocation.getInvocationContext();

		String dismissed  = this.tableModel.getConfigByKey("DismissedHost");
		if (dismissed != null) {
			menu_list.add(new DismissMenu(this));
		}

		menu_list.add(new AddHostToScopeMenu(this));
//		if (!callbacks.getBurpVersion().toString().startsWith("1.")) {
//			menu_list.add(new DoActiveScanMenu(this));
//		}
		//2.0后续版本添加了主动扫描选项，移除该菜单
		menu_list.add(new DoPortScanMenu(this));
		menu_list.add(new OpenWithBrowserMenu(this));
		menu_list.add(new RunSQLMapMenu(this));
		menu_list.add(new ChunkedEncodingMenu(this));

		if (context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {

			if (this.tableModel.getConfigByKey("XSS-Payload")!=null){
				menu_list.add(new InsertXSSMenu(this));
			}

			menu_list.add(new UpdateCookieMenu(this));
			if (this.config.getUsedCookie()!=null){
				menu_list.add(new UpdateCookieWithHistoryMenu(this));
			}

			UpdateHeaderMenu uhmenu = new UpdateHeaderMenu(this);
			List<String> pHeaders = uhmenu.possibleHeaderNames(invocation);
			/*menu_list.add(uhmenu);*/
			if(!pHeaders.isEmpty()) {
				menu_list.add(uhmenu);
			}
		}

		menu_list.add(new SetCookieMenu(this));
		if (this.config.getUsedCookie() != null){
			menu_list.add(new SetCookieWithHistoryMenu(this));
		}


		JMenu Hack_Bar_Menu = new JMenu("^_^ Hack Bar++");
		Hack_Bar_Menu.add(new SQL_Menu(this));
		Hack_Bar_Menu.add(new SQL_Error(this));
		Hack_Bar_Menu.add(new SQli_LoginBypass(this));

		Hack_Bar_Menu.add(new XSS_Menu(this));
		Hack_Bar_Menu.add(new XXE_Menu(this));
		Hack_Bar_Menu.add(new LFI_Menu(this));//learn from this
		Hack_Bar_Menu.add(new SSTI_Menu(this));

		Hack_Bar_Menu.add(new WebShell_Menu(this));
		Hack_Bar_Menu.add(new Reverse_Shell_Menu(this));

		Hack_Bar_Menu.add(new File_Payload_Menu(this));
		Hack_Bar_Menu.add(new Custom_Payload_Menu(this));

		menu_list.add(Hack_Bar_Menu);
		return menu_list;
	}


	@Override
	public String getTabCaption() {
		return ("Knife");
	}


	@Override
	public Component getUiComponent() {
		return this.getContentPane();
	}

	@Override
	public void extensionUnloaded() {
		callbacks.saveExtensionSetting("knifeconfig", getAllConfig());
	}

	@Override
	public String initConfig() {
		config = new Config("default");
		tableModel = new ConfigTableModel();
		return getAllConfig();
	}

	@Override
	public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
		//processHttpMessage(IBurpExtenderCallbacks.TOOL_PROXY,true,message.getMessageInfo());
		//same action will be executed twice! if call processHttpMessage() here.
		//请求和响应到达proxy时，都各自调用一次,如下部分是测试代码，没毛病啊！
		/*		IHttpRequestResponse messageInfo = message.getMessageInfo();
		if (messageIsRequest) {
			byte[] newRequest = CookieUtils.updateCookie(message.getMessageInfo(),"111111111");
			message.getMessageInfo().setRequest(newRequest);
		}else{
			Getter getter = new Getter(helpers);
			List<String> setHeaders = GetSetCookieHeaders("bbb=2222;");
			List<String> responseHeaders = getter.getHeaderList(false,messageInfo);
			byte[] responseBody = getter.getBody(false,messageInfo);
			responseHeaders.addAll(setHeaders);

			byte[] response = helpers.buildHttpMessage(responseHeaders,responseBody);

			messageInfo.setResponse(response);
		}*/
		if (messageIsRequest) {//丢弃干扰请求
			String currentHost =  message.getMessageInfo().getHttpService().getHost();
			if (isDismissedHost(currentHost)){
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DONT_INTERCEPT);
				message.setInterceptAction(IInterceptedProxyMessage.ACTION_DROP);
				message.getMessageInfo().setHighlight("gray");
				message.getMessageInfo().setComment("Dismissed");
				return;
			}
		}

		//当函数第一次被调用时，还没来得及设置cookie，获取到的cookieToSet必然为空。
		HashMap<String, HeaderEntry> cookieToSetMap = config.getSetCookieMap();
		//stderr.println("called"+cookieToSet);
		if (cookieToSetMap != null && !cookieToSetMap.isEmpty()){//第二次调用如果cookie不为空，就走到这里

			IHttpRequestResponse messageInfo = message.getMessageInfo();
			//String CurrentUrl = messageInfo.getHttpService().toString();//这个方法获取到的url包含默认端口！
			Getter getter = new Getter(helpers);
			String CurrentUrl = getter.getShortURL(messageInfo).toString();
			//stderr.println(CurrentUrl+" "+targetUrl);
			HeaderEntry cookieToSet = cookieToSetMap.get(CurrentUrl);
			if (cookieToSet != null){

				String targetUrl = cookieToSet.getTargetUrl();
				String cookieValue = cookieToSet.getHeaderValue();

				if (messageIsRequest) {
					byte[] newRequest = CookieUtils.updateCookie(messageInfo,cookieValue);
					messageInfo.setRequest(newRequest);
				}else {
					List<String> responseHeaders = getter.getHeaderList(false,messageInfo);
					byte[] responseBody = getter.getBody(false,messageInfo);
					List<String> setHeaders = GetSetCookieHeaders(cookieValue);
					responseHeaders.addAll(setHeaders);

					byte[] response = helpers.buildHttpMessage(responseHeaders,responseBody);

					messageInfo.setResponse(response);
					cookieToSetMap.remove(CurrentUrl);//only need to set once
				}
			}

		}else {//第一次调用必然走到这里
			message.setInterceptAction(IInterceptedProxyMessage.ACTION_FOLLOW_RULES_AND_REHOOK);
			//让burp在等待用户完成操作后再次调用，就相当于再次对request进行处理。
			//再次调用，即使走到了这里，也不会再增加调用次数，burp自己应该有控制。
		}

	}

	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		try {
			if (messageIsRequest) {
				Getter getter = new Getter(helpers);

				URL url = getter.getFullURL(messageInfo);
				String host = getter.getHost(messageInfo);
				LinkedHashMap<String, String> headers = getter.getHeaderMap(messageIsRequest,messageInfo);
				byte[] body = getter.getBody(messageIsRequest,messageInfo);

				boolean isRequestChanged = false;

				//remove header
				List<ConfigEntry> configEntries = tableModel.getConfigByType(ConfigEntry.Action_Remove_From_Headers);
				for (ConfigEntry entry : configEntries) {
					String key = entry.getKey();
					if (headers.remove(key) != null) {
						isRequestChanged = true;
					}
				}

				//add/update/append header
				if (toolFlag == (toolFlag & checkEnabledFor())) {
					//if ((config.isOnlyForScope() && callbacks.isInScope(url))|| !config.isOnlyForScope()) {
					if (!config.isOnlyForScope()||callbacks.isInScope(url)){

						List<ConfigEntry> updateOrAddEntries = tableModel.getConfigEntries();
						for (ConfigEntry entry : updateOrAddEntries) {
							String key = entry.getKey();
							String value = entry.getValue();

							if (value.contains("%host")) {
								value = value.replaceAll("%host", host);
								//stdout.println("3333"+value);
							}

							if (value.toLowerCase().contains("%dnslogserver")) {
								String dnslog = tableModel.getConfigByKey("DNSlogServer");
								Pattern p = Pattern.compile("(?u)%dnslogserver");
								Matcher m = p.matcher(value);

								while (m.find()) {
									String found = m.group(0);
									value = value.replaceAll(found, dnslog);
								}
							}

							if (entry.getType().equals(ConfigEntry.Action_Add_Or_Replace_Header) && entry.isEnable()) {
								headers.put(key, value);
								isRequestChanged = true;

							} else if (entry.getType().equals(ConfigEntry.Action_Append_To_header_value) && entry.isEnable()) {
								value = headers.get(key) + value;
								headers.put(key, value);
								isRequestChanged = true;
								//stdout.println("2222"+value);
							} else if (entry.getKey().equalsIgnoreCase("Chunked-AutoEnable") && entry.isEnable()) {
								headers.put("Transfer-Encoding", "chunked");
								isRequestChanged = true;

								try {
									boolean useComment = false;
									if (this.tableModel.getConfigByKey("Chunked-UseComment") != null) {
										useComment = true;
									}
									String lenStr = this.tableModel.getConfigByKey("Chunked-Length");
									int len = 10;
									if (lenStr != null) {
										len = Integer.parseInt(lenStr);
									}
									body = Methods.encoding(body, len, useComment);
								} catch (UnsupportedEncodingException e) {
									stderr.print(e.getStackTrace());
								}
							}
						}


						///proxy function should be here
						//reference https://support.portswigger.net/customer/portal/questions/17350102-burp-upstream-proxy-settings-and-sethttpservice
						String proxy = this.tableModel.getConfigByKey("Proxy-ServerList");
						String mode = this.tableModel.getConfigByKey("Proxy-UseRandomMode");

						if (proxy != null) {//if enable is false, will return null.
							List<String> proxyList = Arrays.asList(proxy.split(";"));//如果字符串是以;结尾，会被自动丢弃

							if (mode != null) {//random mode
								proxyServerIndex = (int) (Math.random() * proxyList.size());
								//proxyServerIndex = new Random().nextInt(proxyList.size());
							} else {
								proxyServerIndex = (proxyServerIndex + 1) % proxyList.size();
							}
							String proxyhost = proxyList.get(proxyServerIndex).split(":")[0].trim();
							int port = Integer.parseInt(proxyList.get(proxyServerIndex).split(":")[1].trim());

							messageInfo.setHttpService(helpers.buildHttpService(proxyhost, port, messageInfo.getHttpService().getProtocol()));

							String method = helpers.analyzeRequest(messageInfo).getMethod();
							headers.put(method, url.toString());
							isRequestChanged = true;
							//success or failed,need to check?
						}

					}
				}
				if (isRequestChanged){
					//set final request
					List<String> headerList = getter.headerMapToHeaderList(headers);
					messageInfo.setRequest(helpers.buildHttpMessage(headerList,body));
				}


				if (isRequestChanged) {
					//debug
					List<String> finalheaders = helpers.analyzeRequest(messageInfo).getHeaders();
					//List<String> finalheaders = editer.getHeaderList();//error here:bodyOffset getted twice are different
					stdout.println(System.lineSeparator() + "//////////edited request by knife//////////////" + System.lineSeparator());
					for (String entry : finalheaders) {
						stdout.println(entry);
					}
				}
			}else {//response

			}
		} catch (Exception e) {
			e.printStackTrace();
			stderr.print(e.getStackTrace());
		}
	}

	public List<String> GetSetCookieHeaders(String cookies){
		if (cookies.startsWith("Cookie: ")){
			cookies = cookies.replaceFirst("Cookie: ","");
		}

		String[] cookieList = cookies.split("; ");
		List<String> setHeaderList= new ArrayList<String>();
		//Set-Cookie: SST_S22__WEB_RIGHTS=SST_S22_JT_RIGHTS_113_9; Path=/
		for (String cookie: cookieList){
			setHeaderList.add(String.format("Set-Cookie: %s; Path=/",cookie));
		}
		return setHeaderList;
	}

	public boolean isDismissedHost(String host){
		String dissmissed  = tableModel.getConfigByKey("DismissedHost");
		if (dissmissed == null) return false;//表示配置被禁用了
		String[] dissmissedHosts = dissmissed.split(",");
		Iterator<String> it = Arrays.asList(dissmissedHosts).iterator();
		while (it.hasNext()){
			String dissmissedHost = it.next().trim();
			if (dissmissedHost.startsWith("*.")){
				dissmissedHost = dissmissedHost.replaceFirst("\\*","");
				if (host.trim().toLowerCase().endsWith(dissmissedHost.toLowerCase())){
					return true;
				}
			}else if (dissmissedHost.equalsIgnoreCase(host.trim())){
				return true;
			}
		}
		return false;
	}

}
