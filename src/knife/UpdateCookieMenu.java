package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;
import burp.Methods;

public class UpdateCookieMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public UpdateCookieMenu(BurpExtender burp){
		this.setText("^_^ Update cookie");
		this.addActionListener(new UpdateCookie_Action(burp,burp.context));
	}
}

class UpdateCookie_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public UpdateCookie_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		byte selectedInvocationContext = invocation.getInvocationContext();

		byte[] selectedRequest = selectedItems[0].getRequest();


		String shorturl = selectedItems[0].getHttpService().toString();
		String latestCookie = getLatestCookieFromHistory(shorturl);

		if (latestCookie == null) {//when host is ip address, need manually input domain to get the cookie
			latestCookie = getLatestCookieFromSpeicified();
		}

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
			
			if (latestCookie.equals(cookie)) {//if cookie is same, request input to find again
				latestCookie = getLatestCookieFromSpeicified();
			}
			
			//update cookie
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
	
	public String getLatestCookieFromSpeicified() {
		String latestCookie = null;
		String domain = Methods.prompt_and_validate_input("update cookie with cookie of ", null);
		String url1 = "";
		String url2 = "";
		String successURL = "";
		try{
			if (domain.startsWith("http://") || domain.startsWith("https://")) {
				url1 = domain;
			}else {
				url1 = "http://"+domain;
				url2 = "https://"+domain;
			}

			try {
				latestCookie = getLatestCookieFromHistory(url1);
				if (latestCookie != null){
					successURL = url1;
				}
			} catch (Exception e) {

			}

			if (latestCookie == null){
				try {
					latestCookie = getLatestCookieFromHistory(url2);
					if (latestCookie != null){
						successURL = url2;
					}
				} catch (Exception e) {

				}
			}

		}catch(NumberFormatException nfe){
			Methods.show_message("Enter proper domain!!!", "Input Not Valid");
		}
		if (latestCookie != null && successURL.startsWith("http")) {
			this.burp.config.getBasicConfigs().put("UsedCookie", successURL+"::::"+latestCookie);
		}
		return latestCookie;
	} 
	
}
