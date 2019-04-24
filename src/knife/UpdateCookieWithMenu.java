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

public class UpdateCookieWithMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public UpdateCookieWithMenu(BurpExtender burp){
		if (burp.config.getTmpMap().containsKey("UsedCookie")) {
			String cookieValue = burp.config.getTmpMap().get("UsedCookie");
			int SpliteratorIndex = cookieValue.indexOf("::::");
			String url = cookieValue.substring(0,SpliteratorIndex);
			String trueCookie = cookieValue.substring(SpliteratorIndex+4);
			this.setText("^_^ Update Cookie ("+url+")");
			this.addActionListener(new UpdateCookieWith_Action(burp,burp.context,trueCookie));
		}
	}
}

class UpdateCookieWith_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;
	private String cookie;

	public UpdateCookieWith_Action(BurpExtender burp,IContextMenuInvocation invocation,String cookie) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
		this.cookie = cookie;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		byte selectedInvocationContext = invocation.getInvocationContext();

		byte[] selectedRequest = selectedItems[0].getRequest();

		String latestCookie = this.cookie;

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