package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;


public class UpdateHeaderMenu extends JMenu {
	//JMenuItem vs. JMenu
	public BurpExtender burp;
	public IContextMenuInvocation invocation;
    public UpdateHeaderMenu(BurpExtender burp){
    	
    	this.invocation = burp.context;
    	this.burp = burp;
    	
    	List<String> pHeaders = possibleHeaderNames(invocation);
        this.setText("^_^ Update Header");
	    for (String pheader:pHeaders) {
	    	JMenuItem headerItem = new JMenuItem(pheader);
	    	headerItem.addActionListener(new UpdateHeader_Action(burp,invocation,pheader));
	    	this.add(headerItem);
	    }
    }

    public List<String> possibleHeaderNames(IContextMenuInvocation invocation) {
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		//byte selectedInvocationContext = invocation.getInvocationContext();
	
		byte[] selectedRequest = selectedItems[0].getRequest();
		List<String> headers = burp.helpers.analyzeRequest(selectedRequest).getHeaders();
		
		List<String> keywords = Arrays.asList(burp.config.getBasicConfigs().get("tokenHeaders").split(","));
		List<String> ResultHeaders = new ArrayList<String>();
		
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

class UpdateHeader_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	
	private String headerName;
	
	public UpdateHeader_Action(BurpExtender burp,IContextMenuInvocation invocation,String headerName) {
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
        this.stdout = burp.stdout;
        this.headerName = headerName;
	}
	

	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		byte selectedInvocationContext = invocation.getInvocationContext();
	
		byte[] selectedRequest = selectedItems[0].getRequest();
		
		
		String shorturl = selectedItems[0].getHttpService().toString();
		String latestCookie = getLatestHeaderFromHistory(shorturl,headerName);
		
		if (latestCookie !=null) {
        	IRequestInfo analyzedRequest = helpers.analyzeRequest(selectedRequest);//只取第一个
        	List<String> headers = analyzedRequest.getHeaders();//a bug here,report to portswigger
        	//callbacks.printOutput(headers.toString());
        	String cookie =null;
        	for(String header:headers) {
        		if(header.toLowerCase().startsWith(headerName.toLowerCase())) {
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

}
