package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import knife.OpenWithBrowserMenu;
import knife.UpdateCookieMenu;
import knife.UpdateCookieWithMenu;
import knife.UpdateHeaderMenu;

public class BurpExtender extends GUI implements IBurpExtender, IContextMenuFactory, IMessageEditorTabFactory, ITab, IHttpListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IContextMenuInvocation context;
	public Getter getter;
	
	public BurpExtender getExtender() {
		return this;
	}
	

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		this.stdout = new PrintWriter(callbacks.getStdout(), true);
		this.stderr = new PrintWriter(callbacks.getStderr(), true);
		this.getter = new Getter(helpers);
		this.callbacks.setExtensionName(this.ExtensionName);
		this.callbacks.registerContextMenuFactory(this);// for menus
		this.callbacks.registerMessageEditorTabFactory(this);// for U2C
		this.callbacks.addSuiteTab(BurpExtender.this);
		this.callbacks.registerHttpListener(this);

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

	}


	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		this.context = invocation;

		ArrayList<JMenuItem> menu_list = new ArrayList<JMenuItem>();


		byte context = invocation.getInvocationContext();
		//只有当选中的内容是响应包的时候才显示U2C

		if (context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
			menu_list.add(new UpdateCookieMenu(this));
			menu_list.add(new UpdateCookieWithMenu(this));
		}


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
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		if (messageIsRequest){
			boolean isHeaderChanaged = false;
			URL url =getter.getURL(messageInfo);
			String host = getter.getHost(messageInfo);
			byte[] body = getter.getBody(true, messageInfo);
			HashMap<String, String> headers = getter.getHeaderHashMap(true, messageInfo);//this will lost the first line

			/*//debug
			stdout.println("//////////original headers before edit//////////////");
			//contains  GET /cps.gec/limit/information.html HTTP/1.1
			List<String> originheaders = getter.getHeaderList(true, messageInfo);
			Iterator<String> oit = originheaders.iterator();
			while(oit.hasNext()){
				String entry = oit.next();
				stdout.println(entry);
			}*/

			//remove header
			ArrayList<String> removeHeaderList = new ArrayList<String>();
			List<ConfigEntry> configEntries = tableModel.getConfigByType(ConfigEntry.Action_Remove_From_Headers);
			Iterator<ConfigEntry> it1 = configEntries.iterator();
			while(it1.hasNext()){
				ConfigEntry entry = it1.next();
				String key = entry.getKey();
				removeHeaderList.add(key);
			}
				
			Iterator<Entry<String, String>> it = headers.entrySet().iterator();
			while(it.hasNext()){
				Entry<String, String> entry = it.next();
				String key = entry.getKey();
				if(removeHeaderList.contains(key)) {
					it.remove();
					isHeaderChanaged =true;
					//debug
					//stdout.println(key+": "+entry.getValue()+" removed");
				}
			}
			
			
			//add/update/append header
			if (toolFlag == (toolFlag&checkEnabledFor())){
				if((config.isOnlyForScope() == true && callbacks.isInScope(url)==true)
						|| config.isOnlyForScope()==false) {
					try{
						List<ConfigEntry> updateOrAddEntries = tableModel.getConfigEntries();
						Iterator<ConfigEntry> it2 = updateOrAddEntries.iterator();
						while(it2.hasNext()){
							ConfigEntry entry = it2.next();
							String key = entry.getKey();
							String value = entry.getValue();

							if (value.contains("%host")){
								value = value.replaceAll("%host", host);
								//stdout.println("3333"+value);
							}

							if(value.toLowerCase().contains("%dnslogserver")) {
								String dnslog = tableModel.getConfigByKey("DNSlogServer");
								Pattern p = Pattern.compile("(?u)%dnslogserver");
								Matcher m  = p.matcher(value);

								while ( m.find() ) {
									String found = m.group(0);
									value = value.replaceAll(found, dnslog);
								}
							}

							if (entry.getKey().equals(ConfigEntry.Action_Add_Or_Replace_Header) && entry.isEnable()) {
								headers.put(key, value);
								isHeaderChanaged = true;
							}else if (entry.getKey().equals(ConfigEntry.Action_Append_To_header_value) && entry.isEnable()) {
								value = headers.get(key)+value;
								headers.put(key, value);
								isHeaderChanaged = true;
								//stdout.println("2222"+value);
							}
						}
					}
					catch(Exception e){
						e.printStackTrace(stderr);
					}
				}
			}
			//set final request
			if (isHeaderChanaged) {
				List<String> Listheaders = getter.MapToList(headers);
				String firstline = getter.getHeaderList(true, messageInfo).get(0);
				Listheaders.add(0, firstline);//add the first line (GET /cps.gec/limit/information.html HTTP/1.1)
				byte[] new_Request = helpers.buildHttpMessage(Listheaders,body);
				messageInfo.setRequest(new_Request);

				//debug
				stdout.println("//////////edited headers by knife//////////////");
				List<String> finalheaders = getter.getHeaderList(true, messageInfo);
				Iterator<String> finalit = finalheaders.iterator();
				while(finalit.hasNext()){
					String entry = finalit.next();
					stdout.println(entry);
				}
			}
		}
	}
}
