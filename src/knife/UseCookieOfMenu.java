package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.JMenuItem;

import burp.*;

public class UseCookieOfMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	
	private static final long serialVersionUID = 1L;

	public UseCookieOfMenu(BurpExtender burp){

    	Getter getter = new Getter(burp.helpers);
    	String currentHost = null;
    	try {
			currentHost = getter.getHost(burp.context.getSelectedMessages()[0]);
		} catch (Exception e) {
			
		}
    	PrintWriter stderr = new PrintWriter(BurpExtender.callbacks.getStderr(), true);
    	stderr.print("currentHost"+currentHost);
    	
		if (currentHost != null && burp.config.getTmpMap().containsKey(currentHost)) {
			String cookieValue = burp.config.getTmpMap().get(currentHost);
			stderr.print("cookieValue");
			String[] values = cookieValue.split("::::");
			String url = values[0];
			this.setText("^_^ Cancel use cookie of "+url);
		}else {
			this.setText("^_^ Use cookie of");
		}
		this.addActionListener(new UseCookieOf_Action(burp,burp.context));
	}
}

class UseCookieOf_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;
	private String tmphost;

	public UseCookieOf_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		MessageEditor editor = new MessageEditor(true,selectedItems[0],helpers);
		this.tmphost = editor.getHost();
		
		if (event.getActionCommand().equals("^_^ Use cookie of")) {
			String lastCookie = getLatestCookieFromSpeicified();

			LinkedHashMap<String, String> headers = new MessageEditor(true, selectedItems[0], helpers).getHeaderMap();
			if (burp.config.getTmpMap().containsKey(tmphost)) {//自动更新cookie
				String cookieValue = burp.config.getTmpMap().get(tmphost);
				String[] values = cookieValue.split("::::");
				String trueCookie = values[1];
				headers.put("Cookie", trueCookie);
			}
			editor.setHeaderMap(headers);
			selectedItems[0] = editor.getMessageInfo();

		}else {// cancel use cookie of http://xxx
			this.burp.config.getTmpMap().remove(this.tmphost);
			LinkedHashMap<String, String> headers = new MessageEditor(true, selectedItems[0], helpers).getHeaderMap();
			headers.remove("Cookie");
			editor.setHeaderMap(headers);
			selectedItems[0] = editor.getMessageInfo();
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
						lastestCookie = hisHeader.replaceFirst("Cookie: ","");
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
			this.burp.config.getTmpMap().put(this.tmphost, successURL+"::::"+latestCookie);
		}
		return latestCookie;
	} 
}
