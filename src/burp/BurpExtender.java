package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import com.alibaba.fastjson.JSON;

import U2C.U2CTab;
import config.Config;
import config.ConfigEntry;
import config.ConfigTable;
import config.ConfigTableModel;
import config.GUI;
import knife.AddHostToScopeMenu;
import knife.ChunkedEncodingMenu;
import knife.OpenWithBrowserMenu;
import knife.UpdateCookieMenu;
import knife.UpdateHeaderMenu;
import knife.UseCookieOfMenu;

public class BurpExtender extends GUI implements IBurpExtender, IContextMenuFactory, IMessageEditorTabFactory, ITab, IHttpListener,IProxyListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IContextMenuInvocation context;
	public int proxyServerIndex=-1;


	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		this.stdout = new PrintWriter(callbacks.getStdout(), true);
		this.stderr = new PrintWriter(callbacks.getStderr(), true);
		callbacks.setExtensionName(this.ExtensionName);
		callbacks.registerContextMenuFactory(this);// for menus
		callbacks.registerMessageEditorTabFactory(this);// for U2C
		callbacks.addSuiteTab(BurpExtender.this);
		callbacks.registerHttpListener(this);

		this.stdout.println(ExtensionName);
		this.stdout.println(github);

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
	}


	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		this.context = invocation;

		ArrayList<JMenuItem> menu_list = new ArrayList<JMenuItem>();


		byte context = invocation.getInvocationContext();
		//只有当选中的内容是响应包的时候才显示U2C

		if (context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
			menu_list.add(new UpdateCookieMenu(this));
			//menu_list.add(new UpdateCookieWithMenu(this));

		}

		menu_list.add(new UseCookieOfMenu(this));

		if (context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST ) {
			UpdateHeaderMenu uhmenu = new UpdateHeaderMenu(this);
			List<String> pHeaders = uhmenu.possibleHeaderNames(invocation);
			/*menu_list.add(uhmenu);*/
			if(!pHeaders.isEmpty()) {
				menu_list.add(uhmenu);
			}
		}

		menu_list.add(new AddHostToScopeMenu(this));
		menu_list.add(new OpenWithBrowserMenu(this));
		menu_list.add(new ChunkedEncodingMenu(this));


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

	//U2C
	@Override
	public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
		return new U2CTab(controller, false, helpers, callbacks);
	}


	@Override
	public String getTabCaption() {
		return ("Knife");
	}


	@Override
	public Component getUiComponent() {
		return this.getContentPane();
	}

	public void saveConfigToExtension() {
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
	}

	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		if (messageIsRequest) {
			Getter getter = new Getter(helpers);

			URL url = getter.getURL(messageInfo);
			String host = getter.getHost(messageInfo);
			String path = url.getPath();
			String firstLineOfHeader = getter.getHeaderFirstLine(messageIsRequest,messageInfo);
			LinkedHashMap headers = getter.getHeaderHashMap(messageIsRequest,messageInfo);
			IHttpService service = messageInfo.getHttpService();
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
					try {
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
									e.printStackTrace(stderr);
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

							firstLineOfHeader = firstLineOfHeader.replaceFirst(path, url.toString().split("\\?",0)[0]);
							isRequestChanged = true;
							//success or failed,need to check?
						}
					} catch (Exception e) {
						e.printStackTrace(stderr);
					}
				}
			}

			//set final request
			List<String> headerList = getter.HeaderMapToList(firstLineOfHeader,headers);
			messageInfo.setRequest(helpers.buildHttpMessage(headerList,body));

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
	}

	@Deprecated
	public void processHttpMessageWithEditor(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		//messageeditor
		synchronized (messageInfo) {
			if (messageIsRequest) {

				boolean isRequestChanged = false;
				MessageEditor editer = new MessageEditor(messageIsRequest, messageInfo, helpers);

				URL url = editer.getURL();
				String path = url.getPath();
				String host = editer.getHost();
				byte[] body = editer.getBody();
				LinkedHashMap<String, String> headers = editer.getHeaderMap();//this will lost the first line


				//remove header
				List<ConfigEntry> configEntries = tableModel.getConfigByType(ConfigEntry.Action_Remove_From_Headers);
				for (ConfigEntry entry : configEntries) {
					String key = entry.getKey();
					if (headers.remove(key) != null) {
						isRequestChanged = true;
					}
				}

				if (config.getTmpMap().containsKey(host)) {//自动更新cookie
					String cookieValue = config.getTmpMap().get(host);
					String[] values = cookieValue.split("::::");
					String trueCookie = values[1];
					headers.put("Cookie", trueCookie);
					isRequestChanged = true;
				}

				//add/update/append header
				if (toolFlag == (toolFlag & checkEnabledFor())) {
					//if ((config.isOnlyForScope() && callbacks.isInScope(url))|| !config.isOnlyForScope()) {
					if (!config.isOnlyForScope()||callbacks.isInScope(url)){
						try {
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
										editer.setBody(body);
									} catch (UnsupportedEncodingException e) {
										e.printStackTrace(stderr);
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
								editer.setService(
										helpers.buildHttpService(proxyhost, port, messageInfo.getHttpService().getProtocol()));
								String firstrline = editer.getFirstLineOfHeader().replaceFirst(path, url.toString().split("\\?",0)[0]);
								editer.setFirstLineOfHeader(firstrline);
								isRequestChanged = true;
								//success or failed,need to check?
							}
						} catch (Exception e) {
							e.printStackTrace(stderr);
						}
					}
				}
				//set final request
				editer.setHeaderMap(headers);
				messageInfo = editer.getMessageInfo();

				if (isRequestChanged) {
					//debug
					List<String> finalheaders = helpers.analyzeRequest(messageInfo).getHeaders();
					//List<String> finalheaders = editer.getHeaderList();//error here:bodyOffset getted twice are different
					stdout.println(System.lineSeparator() + "//////////edited request by knife//////////////" + System.lineSeparator());
					for (String entry : finalheaders) {
						stdout.println(entry);
					}
				}
			}
		}//sync
	}
}
