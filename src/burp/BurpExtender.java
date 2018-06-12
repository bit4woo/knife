package burp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;

import U2C.Unicode;


public class BurpExtender extends Thread implements IBurpExtender, IExtensionStateListener,IContextMenuFactory,IHttpListener
{
	public String ExtenderName = "knife v0.1";
	public String github = "https://github.com/bit4woo/knife";
	public IBurpCollaboratorClientContext ccc;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;//用于输出，主要用于代码调试
	public IBurpExtenderCallbacks callbacks;
	public List<String> dismissUrls; //用于记录不想看到的URL
	//一键复测和让某个URL不在burp http proxy 中显示，这2个功能还无法实现，由于burp本身的限制。
	public GUIU2C U2CWindowFlag;

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
	{
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stdout.println(ExtenderName);
		stdout.println(github);
		this.callbacks=callbacks;
		callbacks.setExtensionName(ExtenderName);
		callbacks.registerExtensionStateListener(this);
		callbacks.registerContextMenuFactory(this);
		ccc = callbacks.createBurpCollaboratorClientContext();
		helpers = callbacks.getHelpers();
		start();
	}

	@Override
	public void extensionUnloaded() {
		stdout.println(ExtenderName+" unloaded!");
	}
	
	@Override
	public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
		// TODO Auto-generated method stub
		if(messageIsRequest) {
    		IRequestInfo analyzedRequest = helpers.analyzeRequest(messageInfo);
    		String fullUrl= analyzedRequest.getUrl().toString();
    		if(dismissUrls.contains(fullUrl)) {
    			//如何让这个请求消失呢？
    			messageInfo.setRequest(null);
    		}
		}
		
	}
	
	
	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
	{ //需要在签名注册！！callbacks.registerContextMenuFactory(this);
	    List<JMenuItem> list = new ArrayList<JMenuItem>();
    	if(true) {//invocation.getToolFlag() == 16
    		
    		JMenuItem menuItem2 = new JMenuItem("^-^ Copy this cookie");
    		menuItem2.addActionListener(new copyThisCookie(invocation));
    		list.add(menuItem2);
    		
    		JMenuItem menuItem = new JMenuItem("^-^ Get lastest cookie");
            menuItem.addActionListener(new getLastestCookie(invocation));
            list.add(menuItem);
            
    		JMenuItem menuItem1 = new JMenuItem("^-^ Add host to scope");//适用于最小scope的情况，
    		menuItem1.addActionListener(new addHostToScope(invocation));
    		list.add(menuItem1);

    		/*
    		JMenuItem menuItem3 = new JMenuItem("^-^ Dismiss");
    		//目前想到的方法是：修改scope，in scope中的是any，将不想看到的加到"exclude from scope";然后启用proxy-options-miscellaneous--don't send to proxy if "out of scope";
    		//但scope的影响很大，比如session的处理，spider的行为等，需要慎重考虑，而且高级的scope操作，api不支持；故暂时搁置这个功能。
    		menuItem3.addActionListener(new disMiss(invocation,dismissUrls));
    		list.add(menuItem3);
    		*/
			byte context = invocation.getInvocationContext();
			//只有当选中的内容是响应包的时候才显示U2C
			if (context == invocation.CONTEXT_MESSAGE_EDITOR_RESPONSE || context == invocation.CONTEXT_MESSAGE_VIEWER_RESPONSE) {
	    		JMenuItem menuItem4 = new JMenuItem("^-^ U2C");
	    		menuItem4.addActionListener(new U2C(invocation));
	    		list.add(menuItem4);
			}

        }
    	return list;
	}
	
	public class getLastestCookie implements ActionListener{
		private IContextMenuInvocation invocation;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public getLastestCookie(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	      {
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
        	IRequestInfo analyzedRequest = helpers.analyzeRequest(messages[0]);
    		String url = analyzedRequest.getUrl().toString();
    		String shortUrl = url.substring(0, url.indexOf("/", 8));//从字符串的第8位开始查找
    		
        	//cookie的path属性是无法从请求的数据包中获取的
        	//List<ICookie> cookies = callbacks.getCookieJarContents();
        	//cookie jar 中的cookie的范围确定，主要是domain和path，不需要关注过期时间，因为cookie jar只保存最新的。
        	//还是草粉师傅说得对，直接从history里面拿最好
        	
        	IHttpRequestResponse[]  historyMessages = Reverse(callbacks.getProxyHistory());
        	//callbacks.printOutput("length of history: "+ historyMessages.length);
        	String lastestCookie =null;
        	for (IHttpRequestResponse historyMessage:historyMessages) {
        		IRequestInfo hisAnalyzedRequest = helpers.analyzeRequest(historyMessage);
        		String hisUrl = hisAnalyzedRequest.getUrl().toString();
        		String hisShortUrl = hisUrl.substring(0, hisUrl.indexOf("/", 8));
        		//callbacks.printOutput(hisShortUrl);
        		
        		if (hisShortUrl.equals(shortUrl)) {
        			List<String> hisHeaders = hisAnalyzedRequest.getHeaders();
        			for (String hisHeader:hisHeaders) {
        				if (hisHeader.toLowerCase().startsWith("cookie:")) {
        					lastestCookie = hisHeader;
        					break;
        				}
        			}
        			if(lastestCookie != null) {
        				break;
        			}
        		}
        	}
        	//获取系统剪切板
        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        	if(lastestCookie !=null) {
        	StringSelection selection = new StringSelection(lastestCookie);
        	//添加文本到系统剪切板
        	clipboard.setContents(selection, null);
        	}

	      }
	
		public IHttpRequestResponse[] Reverse(IHttpRequestResponse[] input){
		    for (int start = 0, end = input.length - 1; start < end; start++, end--) {
		    	IHttpRequestResponse temp = input[end];
		        input[end] = input[start];
		        input[start] = temp;
		    }
		    return input;
		}
	}
	
	
	public class addHostToScope implements ActionListener{
		private IContextMenuInvocation invocation;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public addHostToScope(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {
	       try{
	        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
	        	for(IHttpRequestResponse message:messages) {
	        		IRequestInfo analyzedRequest = helpers.analyzeRequest(message);
	        		URL fullUrl= analyzedRequest.getUrl();
	        		String host = fullUrl.getHost();
	        		int port = fullUrl.getPort();
	        		String protocol = fullUrl.getProtocol();
	        		String url = protocol+"://"+host+":"+port;
					URL shortUrl = new URL(url);
		        	//callbacks.printOutput(shortUrl.toString());
		        	callbacks.includeInScope(shortUrl);
	        	}
	        }
	        catch (Exception e1)
	        {
	            BurpExtender.this.callbacks.printError(e1.getMessage());
	        }
	    }
	}
	
	
	
	public class copyThisCookie implements ActionListener{
		private IContextMenuInvocation invocation;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public copyThisCookie(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {	String cookie = "";
	        try{
	        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
	        	IRequestInfo analyzedRequest = helpers.analyzeRequest(messages[0]);//只取第一个
	        	List<String> headers = analyzedRequest.getHeaders();
	        	for(String header:headers) {
	        		if(header.toLowerCase().startsWith("cookie:")) {
	        			cookie = header;
	        		}
	        	}
	        	
	        	Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	        	StringSelection selection = new StringSelection(cookie);
	        	clipboard.setContents(selection, null);
	        }
	        catch (Exception e1)
	        {
	            BurpExtender.this.callbacks.printError(e1.getMessage());
	        }
	    }
	}
	
	
	public class disMiss implements ActionListener{
		private IContextMenuInvocation invocation;
		private List<String> dismissUrls;
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public disMiss(IContextMenuInvocation invocation, List<String> dismissUrls) {
			this.invocation  = invocation;
			this.dismissUrls = dismissUrls;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {
	       try{
	        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
	        	for(IHttpRequestResponse message:messages) {
	        		IRequestInfo analyzedRequest = helpers.analyzeRequest(message);
	        		String fullUrl= analyzedRequest.getUrl().toString();
	        		dismissUrls.add(fullUrl);
	        	}
	        }
	        catch (Exception e1)
	        {
	            BurpExtender.this.callbacks.printError(e1.getMessage());
	        }
	    }
	}

	
	public class U2C implements ActionListener{
		private IContextMenuInvocation invocation;
		
		//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
		public U2C(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		@Override
		public void actionPerformed(ActionEvent e)
	    {	
			try {

				int[] indexs = invocation.getSelectionBounds();
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();

				byte[] req = messages[0].getResponse();
				String selected =new String(req).substring(indexs[0], indexs[1]);
				//callbacks.printOutput(new String(req));
				//callbacks.printOutput(selected);
				
				selected = selected.replace("\"","\\\"");
                selected = Unicode.unicodeDecode(selected);
                
	        	//IRequestInfo analyzedRequest = helpers.analyzeRequest(messages[0]);//只取第一个
				if (U2CWindowFlag == null) {
					GUIU2C window = new GUIU2C();
					window.frame.setVisible(true);
					window.u2cTextEditor.setText(selected.getBytes());
				}else {
					U2CWindowFlag.frame.setVisible(true);
					U2CWindowFlag.u2cTextEditor.setText(selected.getBytes());
				}

				
				//String newStr = new String(str.getBytes("GB2312"),"ISO-8859-1");
				
				invocation.getSelectionBounds();
			} catch (Exception ex) {
				//ex.printStackTrace();
				callbacks.printError(ex.getMessage());
			}
	    }
	}

	public class GUIU2C {
		public JFrame frame;
		public ITextEditor u2cTextEditor;
	
		/**
		 * Create the application.
		 */
		public GUIU2C() {
			initialize();
		}

		/**
		 * Initialize the contents of the frame.
		 */
		private void initialize() {
			frame = new JFrame();
			frame.setTitle("Unicode2Chinese");
			frame.setBounds(100, 100, 832, 300);
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	U2CWindowFlag = null;
	        }
	        @Override
	        public void windowClosed(WindowEvent e) {
	        }
	    });
			
		    u2cTextEditor = callbacks.createTextEditor();
		    //u2cTextEditor.setText("xxxxx".getBytes());
		    u2cTextEditor.setEditable(false);
			frame.getContentPane().add(u2cTextEditor.getComponent(), BorderLayout.CENTER);
			U2CWindowFlag = this;//将自生传递为全局变量，关闭时设为null，保证只有一个窗口被创建。
		}
	}

}
