package burp;

import java.awt.Component;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.google.gson.Gson;

import U2C.ChineseTabFactory;
import config.Config;
import config.ConfigEntry;
import config.ConfigTable;
import config.ConfigTableModel;
import config.GUI;
import knife.AddHostToScopeMenu;
import knife.ChunkedEncodingMenu;
import knife.CustomPayloadForAllInsertpointMenu;
import knife.CustomPayloadMenu;
import knife.DismissCancelMenu;
import knife.DismissMenu;
import knife.DoActiveScanMenu;
import knife.DoPortScanMenu;
import knife.DownloadResponseMenu;
import knife.FindUrlAndRequest;
import knife.OpenWithBrowserMenu;
import knife.RunSQLMapMenu;
import knife.SetCookieMenu;
import knife.SetCookieWithHistoryMenu;
import knife.UpdateCookieMenu;
import knife.UpdateCookieWithHistoryMenu;
import knife.UpdateHeaderMenu;
import manager.ChunkManager;
import manager.DismissedTargetsManager;
import manager.HeaderManager;

public class BurpExtender extends GUI implements IBurpExtender, IContextMenuFactory, ITab, IHttpListener,IProxyListener,IExtensionStateListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static IBurpExtenderCallbacks callbacks;
	public IExtensionHelpers helpers;
	public static PrintWriter stdout;
	public static PrintWriter stderr;
	public IContextMenuInvocation invocation;
	public int proxyServerIndex=-1;

	public static String ExtensionName = "Knife";
	public static String Version = bsh.This.class.getPackage().getImplementationVersion();
	public static String Author = "by bit4woo";
	public static String github = "https://github.com/bit4woo/knife";

	public static String CurrentProxy = "";

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
		BurpExtender.callbacks = callbacks;
		this.helpers = callbacks.getHelpers();
		flushStd();
		BurpExtender.stdout.println(getFullExtensionName());
		BurpExtender.stdout.println(github);

		table = new ConfigTable(new ConfigTableModel());
		configPanel.setViewportView(table);

		String content = callbacks.loadExtensionSetting("knifeconfig");
		if (content!=null) {
			config = new Gson().fromJson(content, Config.class);
			showToUI(config);
		}else {
			showToUI(new Gson().fromJson(initConfig(), Config.class));
		}
		table.setupTypeColumn();//call this function must after table data loaded !!!!
		table.tableHeaderLengthInit();


		ChineseTabFactory chntabFactory = new ChineseTabFactory(null, false, helpers, callbacks);

		//各项数据初始化完成后在进行这些注册操作，避免插件加载时的空指针异常
		callbacks.setExtensionName(getFullExtensionName());
		callbacks.registerContextMenuFactory(this);// for menus
		callbacks.registerMessageEditorTabFactory(chntabFactory);// for Chinese
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

	//name+version+author
	public static String getFullExtensionName(){
		return ExtensionName+" "+Version+" "+Author;
	}

	//JMenu 是可以有下级菜单的，而JMenuItem是不能有下级菜单的
	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
		ArrayList<JMenuItem> menu_item_list = new ArrayList<JMenuItem>();

		this.invocation = invocation;
		//常用
		menu_item_list.add(new OpenWithBrowserMenu(this));
		menu_item_list.add(new CustomPayloadMenu(this));
		menu_item_list.add(new CustomPayloadForAllInsertpointMenu(this));

		//cookie身份凭证相关
		menu_item_list.add(new UpdateCookieMenu(this));
		menu_item_list.add(new UpdateCookieWithHistoryMenu(this));

		menu_item_list.add(new SetCookieMenu(this));
		menu_item_list.add(new SetCookieWithHistoryMenu(this));

		UpdateHeaderMenu updateHeader = new UpdateHeaderMenu(this);//JMenuItem vs. JMenu
		if (updateHeader.getItemCount()>0) {
			menu_item_list.add(updateHeader);
		}

		//扫描攻击相关
		menu_item_list.add(new AddHostToScopeMenu(this));
		menu_item_list.add(new RunSQLMapMenu(this));
		menu_item_list.add(new DoActiveScanMenu(this));
		menu_item_list.add(new DoPortScanMenu(this));


		//不太常用的
		menu_item_list.add(new DismissMenu(this));
		menu_item_list.add(new DismissCancelMenu(this));

		menu_item_list.add(new ChunkedEncodingMenu(this));
		menu_item_list.add(new DownloadResponseMenu(this));
		//menu_item_list.add(new DownloadResponseMenu2(this));
		//menu_item_list.add(new ViewChineseMenu(this));
		//menu_item_list.add(new JMenuItem());
		//空的JMenuItem不会显示，所以将是否添加Item的逻辑都方法到类当中去了，以便调整菜单顺序。

		menu_item_list.add(new FindUrlAndRequest(this));

		Iterator<JMenuItem> it = menu_item_list.iterator();
		while (it.hasNext()) {
			JMenuItem item = it.next();
			if (item.getText()==null || item.getText().equals("")) {
				it.remove();
			}
		}

		String oneMenu  = tableModel.getConfigValueByKey("Put_MenuItems_In_One_Menu");
		if (oneMenu != null) {
			ArrayList<JMenuItem> Knife = new ArrayList<JMenuItem>();
			JMenu knifeMenu = new JMenu("^_^ Knife");
			Knife.add(knifeMenu);
			for (JMenuItem item : menu_item_list) {
				knifeMenu.add(item);
			}
			return Knife;
		}else {
			return menu_item_list;
		}
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

	//IProxyListener中的方法，修改的内容会在proxy中显示为edited
	@Override
	public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message) {
		//processHttpMessage(IBurpExtenderCallbacks.TOOL_PROXY,true,message.getMessageInfo());
		//same action will be executed twice! if call processHttpMessage() here.
		if (CurrentProxy == null || CurrentProxy.equals("")) {
			//为了知道burp当前监听的接口。供“find url and request”菜单使用
			CurrentProxy = message.getListenerInterface();
		}

		if (messageIsRequest) {
			boolean dropped = DismissedTargetsManager.checkDropAction(messageIsRequest, message);//只在proxy中
			if (dropped) {
				return;
			}

			IHttpRequestResponse messageInfo =message.getMessageInfo();

			List<ConfigEntry> rules = DismissedTargetsManager.getAllChangeActionExceptDropRules();
			for (int index=rules.size()-1;index>=0;index--) {//按照时间倒叙引用规则

				ConfigEntry rule = rules.get(index);

				if (rule.isForwardActionType()) {
					DismissedTargetsManager.checkForwardAction(rule, messageIsRequest, message);//只在proxy中
				}

				if (rule.isScopeBasedHeaderHandleActionType()) {
					if (isInScope(IBurpExtenderCallbacks.TOOL_PROXY,messageInfo)) {
						HeaderManager.checkScopeBasedRuleAndTakeAction(rule, messageIsRequest, messageInfo);
					}
				}

				if (rule.isHeaderHandleWithIfActionType()) {
					HeaderManager.checkURLBasedRuleAndTakeAction(rule, messageIsRequest, messageInfo);
				}
			}

			if (isInScope(IBurpExtenderCallbacks.TOOL_PROXY,messageInfo)) {
				ChunkManager.doChunk(messageIsRequest, messageInfo);
			}
		}
	}

	//IHttpListener中的方法，修改的内容在Proxy中不可见
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		//stdout.println("processHttpMessage called when messageIsRequest="+messageIsRequest);
		try {
			if (messageIsRequest) {
				//add/update/append header
				if (toolFlag == IBurpExtenderCallbacks.TOOL_PROXY) {
					//##############################//
					//handle it in processProxyMessage(). so we can see the changes in the proxy view.
					//##############################//
				}else {
					List<ConfigEntry> rules = HeaderManager.getAllChangeRules();
					for (int index=rules.size()-1;index>=0;index--) {//按照时间倒叙引用规则

						ConfigEntry rule = rules.get(index);

						if (rule.isScopeBasedHeaderHandleActionType()) {
							if (isInScope(toolFlag,messageInfo)) {
								HeaderManager.checkScopeBasedRuleAndTakeAction(rule, messageIsRequest, messageInfo);
							}
						}

						if (rule.isHeaderHandleWithIfActionType()) {
							HeaderManager.checkURLBasedRuleAndTakeAction(rule, messageIsRequest, messageInfo);
						}
					}

					if (isInScope(IBurpExtenderCallbacks.TOOL_PROXY,messageInfo)) {
						ChunkManager.doChunk(messageIsRequest, messageInfo);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			stderr.print(e.getStackTrace());
		}
	}

	public static void confirmProxy() {
		String proxy = JOptionPane.showInputDialog("Confirm Proxy Of Burp", "127.0.0.1:8080");
		if (proxy != null) {
			BurpExtender.CurrentProxy = proxy.trim();
		}
	}

	public static String getProxyHost() {
		try {
			if (CurrentProxy == null ||CurrentProxy.equals("") || CurrentProxy.split(":").length!=2) {
				confirmProxy();
			}
			String proxyHost = CurrentProxy.split(":")[0];
			return proxyHost;
		} catch (Exception e) {
			e.printStackTrace();
			CurrentProxy="";//设置为空，以便重新获取。
			return null;
		}
	}

	public static int getProxyPort() {
		try {
			if (CurrentProxy == null ||CurrentProxy.equals("") || CurrentProxy.split(":").length!=2) {
				confirmProxy();
			}
			String proxyPort = CurrentProxy.split(":")[1];
			return Integer.parseInt(proxyPort);
		} catch (Exception e) {
			e.printStackTrace();
			CurrentProxy="";//设置为空，以便重新获取。
			return -1;
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

	public static IBurpExtenderCallbacks getCallbacks() {
		return callbacks;
	}


	public boolean isInScope(int toolflag,IHttpRequestResponse messageInfo) {
		if (toolflag == (toolflag & checkEnabledFor())) {

			IExtensionHelpers helpers = BurpExtender.getCallbacks().getHelpers();
			URL url = new HelperPlus(helpers).getFullURL(messageInfo);

			if (!config.isOnlyForScope()||callbacks.isInScope(url)){
				return true;
			}
		}
		return false;
	}


}
