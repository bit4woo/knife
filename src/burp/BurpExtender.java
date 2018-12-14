package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import U2C.U2CTab;


public class BurpExtender extends Thread implements IBurpExtender, IExtensionStateListener,IContextMenuFactory, IMessageEditorTabFactory
{
	public String ExtenderName = "knife v0.7";
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
		callbacks.registerMessageEditorTabFactory(this);
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
		    JMenuItem menuItemUpdateCookie = new JMenuItem("^_^ Update cookie");
			menuItemUpdateCookie.addActionListener(new updateHeader(invocation,"cookie"));	
			list.add(menuItemUpdateCookie);
		}
		
		List<String> pHeaders = possibleHeaderNames(invocation);
		if (context == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST && !pHeaders.isEmpty()) {
		    JMenu menuItemUpdateHeader = new JMenu("^_^ Update Header");
		    for (String pheader:pHeaders) {
		    	JMenuItem headerItem = new JMenuItem(pheader);
		    	headerItem.addActionListener(new updateHeader(invocation,pheader));
		    	menuItemUpdateHeader.add(headerItem);
		    }
			//menuItemUpdateCookie.addActionListener(new updateHeader(invocation,"cookie"));	
			list.add(menuItemUpdateHeader);
		}
	    
		
		JMenuItem menuItemAddHostScope = new JMenuItem("^_^ Add host to scope");//适用于最小scope的情况，
		menuItemAddHostScope.addActionListener(new addHostToScope(invocation));
		list.add(menuItemAddHostScope);

    	return list;
	}
	

		
	@Override
	public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
		return new U2CTab(controller, false, helpers, callbacks);
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

	public class updateHeader implements ActionListener {
		private IContextMenuInvocation invocation;
		private String headerName;

		public updateHeader(IContextMenuInvocation invocation,String headerName) {
			this.invocation  = invocation;
			this.headerName = headerName;
		}
		
		@Override
		public void actionPerformed(ActionEvent event) {
			
			IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
			byte selectedInvocationContext = invocation.getInvocationContext();
		
			byte[] selectedRequest = selectedItems[0].getRequest();
			String shorturl = selectedItems[0].getHttpService().toString();
			String latestHeader = getLatestHeaderFromHistory(shorturl,headerName);
			
			if (latestHeader !=null) {
	        	IRequestInfo analyzedRequest = helpers.analyzeRequest(selectedRequest);//只取第一个
	        	List<String> headers = analyzedRequest.getHeaders();//a bug here,report to portswigger
	        	//callbacks.printOutput(headers.toString());
	        	String oldHeader =null;
	        	for(String header:headers) {
	        		if(header.toLowerCase().startsWith(headerName.toLowerCase())) {
	        			oldHeader = header;
	        			break;
	        		}
	        	}
        		if(oldHeader !=null) {
        			int index = headers.indexOf(oldHeader);
        			headers.remove(index);
    	        	headers.add(index,latestHeader);
        		}else {
        			headers.add(latestHeader);
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
	
	public String getLatestHeaderFromHistory(String shortUrl,String headerName){
    	//还是草粉师傅说得对，直接从history里面拿最好
    	
    	IHttpRequestResponse[]  historyMessages = Reverse(callbacks.getProxyHistory());
    	//callbacks.printOutput("length of history: "+ historyMessages.length);

    	for (IHttpRequestResponse historyMessage:historyMessages) {
    		IRequestInfo hisAnalyzedRequest = helpers.analyzeRequest(historyMessage);
    		//String hisShortUrl = hisUrl.substring(0, hisUrl.indexOf("/", 8));
    		String hisShortUrl = historyMessage.getHttpService().toString();
    		//callbacks.printOutput(hisShortUrl);
    		
    		if (hisShortUrl.equals(shortUrl)) {
    			List<String> hisHeaders = hisAnalyzedRequest.getHeaders();
    			for (String hisHeader:hisHeaders) {
    				if (hisHeader.toLowerCase().startsWith(headerName.toLowerCase()) && hisHeader.length()>headerName.length()+1) {
    					//callbacks.printOutput(hisHeader);
            			return hisHeader;
    				}
    			}
    		}
    	}
		return null;
	}
	
	public List<String> possibleHeaderNames(IContextMenuInvocation invocation){
		
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		//byte selectedInvocationContext = invocation.getInvocationContext();
	
		byte[] selectedRequest = selectedItems[0].getRequest();
		List<String> headers = helpers.analyzeRequest(selectedRequest).getHeaders();
		
		List<String> keywords = new ArrayList<String>();
		List<String> ResultHeaders = new ArrayList<String>();
		keywords.add("token");
		keywords.add("Authorization");
		keywords.add("Auth");
		
		Iterator<String> it = headers.iterator();
		while (it.hasNext()) {
			String item = it.next();
			String itemName = item.split(":",0)[0];
			if (containOneOfKeywords(itemName,keywords,false)) {
				ResultHeaders.add(itemName.toLowerCase());
			}
		}
		return ResultHeaders;
		
	}
	
	
	public boolean containOneOfKeywords(String x,List<String> keywords,boolean isCaseSensitive) {
		for (String keyword:keywords) {
			if (isCaseSensitive == false) {
				x = x.toLowerCase();
				keyword = keyword.toLowerCase();
			}
			if (x.contains(keyword)){
				return true;
			}
		}
		return false;
	}
}

	
