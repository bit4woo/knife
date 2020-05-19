package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.RobotInput;
import burp.Utils;

public class OpenWithBrowserMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public OpenWithBrowserMenu(BurpExtender burp){
		this.setText("^_^ Open with browser");
		this.addActionListener(new OpenWithBrowser_Action(burp,burp.invocation));
	}
}

class OpenWithBrowser_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public OpenWithBrowser_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try{
			String browserPath = burp.tableModel.getConfigValueByKey("browserPath");
			if (browserPath!=null && new File(browserPath).exists() && new File(browserPath).isFile()) {

			}else {//when no browserPath in config, the value will be null
				browserPath = "default";
			}
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();

			if (messages == null ) {
				return;
			}

			if (messages.length == 1) {
				String selectedUrl = getSelectedStringByBurp();
				//String selectedUrl = new RobotInput().getSelectedString();//为了解决burp API中的bug，尝试用复制粘贴方法获取选中的内容。
				//不知道为什么这里获取到的结果始终是上一次复制的内容。
				//难道是因为，当用鼠标点击右键菜单时，当前的选中内容不是burp数据表中的字符串，而是当前菜单项？所以这个方法走不通了？
				
				
				
				stderr.println("selected URL: "+selectedUrl);
				if (selectedUrl.length()>10) {// http://a.cn
					Utils.browserOpen(selectedUrl,browserPath);
					//stdout.println(selectedUrl);
				}else {
					String hosturl =helpers.analyzeRequest(messages[0]).getUrl().toString();
					Utils.browserOpen(hosturl,browserPath);
				}
			}else if (messages.length > 1 &&  messages.length <=50) {
				for(IHttpRequestResponse message:messages) {
					Getter getter = new Getter(helpers);
					URL targetShortUrl = getter.getFullURL(message);
					Utils.browserOpen(targetShortUrl,browserPath);
				}
			}else {
				stderr.println("Please Select Less URLs to Open");
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
	
	//只适用于GBK编码格式，UTF-8的格式中的结果是它的结果除以3？？？
	public static int ChineseCount(byte[] input) {
		int num = 0;
		for (int i = 0; i < input.length; i++) {
			if (input[i] < 0) {
				num++;
				i = i + 1;
			}
		}
		return num;
	}
	
	public int isChinese(String a)  {
		char[] c = a.toCharArray();
		int count=0;
		for (char d : c) {
			Character.UnicodeBlock ub = Character.UnicodeBlock.of(d);
			if (ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
					|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
					|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
					|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS
					|| ub == Character.UnicodeBlock.VERTICAL_FORMS
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
					|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D
					|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
					|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT) {
				   count++;
			} 
 
		}
		 return count;
	}

	public String getSelectedStringByBurp(){
		String result = "";

		IHttpRequestResponse[] messages = invocation.getSelectedMessages();

		if (messages == null ) {
			return result;
		}

		if (messages.length == 1) {
			IHttpRequestResponse message = messages[0];
			/////////////selected url/////////////////
			byte[] source = null;


			int context = invocation.getInvocationContext();
			if (context==IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST
					|| context ==IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST
					|| context == IContextMenuInvocation.CONTEXT_PROXY_HISTORY
					|| context == IContextMenuInvocation.CONTEXT_INTRUDER_ATTACK_RESULTS
					|| context == IContextMenuInvocation.CONTEXT_SEARCH_RESULTS
					|| context == IContextMenuInvocation.CONTEXT_TARGET_SITE_MAP_TABLE
					|| context == IContextMenuInvocation.CONTEXT_TARGET_SITE_MAP_TREE) {
				source = message.getRequest();
			}else {
				source = message.getResponse();
			}
			
			stderr.println("chinese count: "+isChinese(new String(source)));

			int[] selectedIndex = invocation.getSelectionBounds();//当数据包中有中文或其他宽字符的时候，这里的返回值不正确。已报bug。
			//获得的index根据选中内容前面中文字数的个数*2 的值前移了。
			//stdout.println(selectedIndex[0]+":"+selectedIndex[1]);

			if(source!=null && selectedIndex !=null && selectedIndex[1]-selectedIndex[0]>=3) {
				int selectedLength = selectedIndex[1]-selectedIndex[0];
				byte[] selectedBytes = new byte[selectedLength];
				System.arraycopy(source, selectedIndex[0], selectedBytes, 0, selectedLength);//新的内容替换选中内容
				stderr.println("11--->"+burp.callbacks.getHelpers().bytesToString(selectedBytes));
				stderr.println("22--->"+ new String(selectedBytes));
				result = new String(selectedBytes).trim();
				
			}

			if(!isFullUrl(result)) {
				result = message.getHttpService().toString()+"/"+result;
			}else if(result.startsWith("//")) {
				result = message.getHttpService().getProtocol()+":"+result;
			}
		}
		return result;
	}


	public static boolean isFullUrl(String url) {
		if (url.startsWith("//") || url.startsWith("http://") || url.startsWith("https://")) {//    //misc.360buyimg.com/
			return true;
		}else if (url.startsWith("../") || url.startsWith("./") || url.startsWith("/")) {
			return false;
		}else {
			return true;
		}
	}
}
