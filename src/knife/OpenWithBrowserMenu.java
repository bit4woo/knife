package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.JMenuItem;

import com.bit4woo.utilbox.burp.HelperPlus;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.Utils;
import messageTab.U2C.CharSetHelper;

public class OpenWithBrowserMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public OpenWithBrowserMenu(BurpExtender burp){
		this.setText("^_^ Open With Browser");
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
				//stderr.println("selected URL: "+selectedUrl);
				//stderr.println("selected URL old: "+getSelectedStringByBurpOld());
				
				if (selectedUrl.length()>10) {// http://a.cn
					stdout.println();
					stdout.println("//////////open URL: "+selectedUrl+" //////////");
					Utils.browserOpen(selectedUrl,browserPath);
					//stdout.println(selectedUrl);
				}else {
					String hosturl =helpers.analyzeRequest(messages[0]).getUrl().toString();
					Utils.browserOpen(hosturl,browserPath);
				}
			}else if (messages.length > 1 &&  messages.length <=50) {
				for(IHttpRequestResponse message:messages) {
					HelperPlus getter = new HelperPlus(helpers);
					URL targetShortUrl = getter.getFullURL(message);
					Utils.browserOpen(targetShortUrl,browserPath);
				}
			}else {
				stderr.println("Please Select Less URLs to Open");
			}
		}
		catch (java.net.URISyntaxException e) {
			stderr.println(e.getMessage());
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
	
	@Deprecated
	public String getSelectedStringByBurpOld(){
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

			result = getFullUrl(result,message);
		}
		return result;
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

			int[] selectedIndex = invocation.getSelectionBounds();//当数据包中有中文或其他宽字符的时候，这里的返回值不正确。已报bug。
			//stdout.println(selectedIndex[0]+":"+selectedIndex[1]);
			//这里的index应该是字符串的index，进行选中操作时对象应该是字符文本内容，无论是一个中文还是一个字母，都是一个文本字符。这就是我们通常的文本操作啊，之前是想多了。
			//burp进行的byte和string之间的转换，没有考虑特定的编码，是一刀切的方式，所以将index用于byte序列上，就不能正确对应。

			if(source!=null && selectedIndex !=null && selectedIndex[1]-selectedIndex[0]>=3) {
				String originalCharSet = CharSetHelper.detectCharset(source);
				String text;
				try {
					text = new String(source,originalCharSet);
				}catch(Exception e) {
					text = new String(source);
				}
				result = text.substring(selectedIndex[0], selectedIndex[1]);
				result = getFullUrl(result,message);
			}
		}
		return result;
	}
	
	//<script src="http://lbs.sf-express.com/api/map?v=2.0&ak=b1cfb18ca6864e46b3ed4cb18f12c0f8">
	//<script type=text/javascript src=./static/js/manifest.c7ad14f4845199970dcb.js>
	//<link rel="stylesheet" type="text/css" href="/cat/assets/css/bootstrap.min.css">
	//<link href=static/css/chunk-03d2ee16.a3503987.css rel=prefetch>
	//<link href="www.microsoft.com">这只会被当成目标，不会被当成域名
	//<link href="//www.microsoft.com">会被当成域名，协议会使用当前页面所使用的协议
	public static String getFullUrl(String url,IHttpRequestResponse message) {
		if (url.startsWith("http://") || url.startsWith("https://")) {
			//都是带有host的完整URL，直接访问即可
			return url;
			
		}else if(url.startsWith("//")) {//使用当前web的请求协议
			
			return message.getHttpService().getProtocol()+":"+url;

		}else if (url.startsWith("../") || url.startsWith("./") ) {
			
			return message.getHttpService().toString()+"/"+url;
			
		}else if(url.startsWith("/")){
			
			return message.getHttpService().toString()+url;
			
		}else{//没有斜杠的情况。<link href="www.microsoft.com">这只会被当成目标，不会被当成域名
			
			HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
			String fullUrl = getter.getFullURL(message).toString().split("\\?")[0];
			int indexOfLastSlash = fullUrl.lastIndexOf("/");//截取的内容不不包含当前index对应的元素
			return fullUrl.substring(0,indexOfLastSlash+1)+url;
		}
	}
}
