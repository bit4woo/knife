package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;


public class BurpExtender extends Thread implements IBurpExtender, IExtensionStateListener,IContextMenuFactory
{
	public String ExtenderName = "knife v0.5";
	public String github = "https://github.com/bit4woo/knife";
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//一键复测和让某个URL不在burp http proxy 中显示，这2个功能还无法实现，由于burp本身的限制。

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks)
	{
		stdout = new PrintWriter(callbacks.getStdout(), true);
		stderr = new PrintWriter(callbacks.getStderr(), true);
		stdout.println(ExtenderName);
		stdout.println(github);
		this.callbacks=callbacks;
		callbacks.setExtensionName(ExtenderName);
		callbacks.registerExtensionStateListener(this);
		callbacks.registerContextMenuFactory(this);
		helpers = callbacks.getHelpers();
	}

	@Override
	public void extensionUnloaded() {
		stdout.println(ExtenderName+" unloaded!");
	}
	
	
	@Override
	public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation)
	{ //需要在签名注册！！callbacks.registerContextMenuFactory(this);
	    List<JMenuItem> list = new ArrayList<JMenuItem>();
	    
		byte context = invocation.getInvocationContext();
		//只有当选中的内容是响应包的时候才显示U2C
		if (context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
		    JMenuItem menuItemUpdateCookie = new JMenuItem("^-^ Update cookie");
			menuItemUpdateCookie.addActionListener(new updateCookie(invocation));	
			list.add(menuItemUpdateCookie);
		}
	    
		
		JMenuItem menuItemAddHostScope = new JMenuItem("^_^ Add host to scope");//适用于最小scope的情况，
		menuItemAddHostScope.addActionListener(new addHostToScope(invocation));
		list.add(menuItemAddHostScope);

    	return list;
	}
	
	
	public class addHostToScope implements ActionListener{
		//scope matching is actually String matching!!
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
	        		String url = message.getHttpService().toString();
					URL shortUrl = new URL(url);
		        	callbacks.includeInScope(shortUrl);
	        	}
	        }
	        catch (Exception e1)
	        {
	            e1.printStackTrace(stderr);
	        }
	    }
	}

	public class updateCookie implements ActionListener{
		private IContextMenuInvocation invocation;

		public updateCookie(IContextMenuInvocation invocation) {
			this.invocation  = invocation;
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
			byte selectedInvocationContext = invocation.getInvocationContext();
		
			byte[] selectedRequest = selectedItems[0].getRequest();
			
			
			String shorturl = selectedItems[0].getHttpService().toString();
			String latestCookie = getLatestCookieFromHistory(shorturl);
			
			if (latestCookie !=null) {
	        	IRequestInfo analyzedRequest = helpers.analyzeRequest(selectedRequest);//只取第一个
	        	List<String> headers = analyzedRequest.getHeaders();//a bug here,report to portswigger
	        	//callbacks.printOutput(headers.toString());
	        	String cookie =null;
	        	for(String header:headers) {
	        		if(header.toLowerCase().startsWith("cookie:")) {
	        			cookie = header;
	        			break;
	        		}
	        	}
        		if(cookie !=null) {
        			int index = headers.indexOf(cookie);
        			headers.remove(index);
    	        	headers.add(index,latestCookie);
        		}else {
        			headers.add(latestCookie);
        		}
        		//callbacks.printOutput(headers.toString());
	        	
	        	
	        	byte[] body= Arrays.copyOfRange(selectedRequest, analyzedRequest.getBodyOffset(), selectedRequest.length);
	        	//https://github.com/federicodotta/HandyCollaborator/blob/master/src/main/java/burp/BurpExtender.java
	        	byte[] newRequestBytes = helpers.buildHttpMessage(headers, body);
				
				if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					selectedItems[0].setRequest(newRequestBytes);
				}
			}
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
	
	public String getLatestCookieFromHistory(String shortUrl){
    	//还是草粉师傅说得对，直接从history里面拿最好
    	
    	IHttpRequestResponse[]  historyMessages = Reverse(callbacks.getProxyHistory());
    	//callbacks.printOutput("length of history: "+ historyMessages.length);
    	String lastestCookie =null;
    	for (IHttpRequestResponse historyMessage:historyMessages) {
    		IRequestInfo hisAnalyzedRequest = helpers.analyzeRequest(historyMessage);
    		//String hisShortUrl = hisUrl.substring(0, hisUrl.indexOf("/", 8));
    		String hisShortUrl = historyMessage.getHttpService().toString();
    		//callbacks.printOutput(hisShortUrl);
    		
    		if (hisShortUrl.equals(shortUrl)) {
    			List<String> hisHeaders = hisAnalyzedRequest.getHeaders();
    			for (String hisHeader:hisHeaders) {
    				if (hisHeader.toLowerCase().startsWith("cookie:")) {
    					lastestCookie = hisHeader;
            			if(lastestCookie != null) {
            				return lastestCookie;
            			}
    				}
    			}
    		}
    	}
		return null;
	}
}
