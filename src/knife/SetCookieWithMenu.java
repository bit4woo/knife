package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.JMenuItem;

import burp.*;

public class SetCookieWithMenu extends JMenuItem {
	//JMenuItem vs. JMenu

	private static final long serialVersionUID = 1L;

	public SetCookieWithMenu(BurpExtender burp){
		this.setText("^_^ Set Cookie With");
		this.addActionListener(new SetCookieWith_Action(burp,burp.context));
	}
}

class SetCookieWith_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public SetCookieWith_Action(BurpExtender burp,IContextMenuInvocation invocation) {
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
		Getter getter = new Getter(helpers);
		String host = getter.getHost(selectedItems[0]);
		String cookieToSet = getLatestCookieFromSpeicified();

		this.burp.config.getTmpMap().put("cookieToSet", host+"::::"+cookieToSet);
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
		return latestCookie;
	}
}
