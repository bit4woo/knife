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

public class UpdateCookieWithHistoryMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public UpdateCookieWithHistoryMenu(BurpExtender burp){
		if (burp.config.getTmpMap().containsKey("UsedCookie")) {
			String urlAndcookieValue = burp.config.getTmpMap().get("UsedCookie");
			String url = urlAndcookieValue.split(CookieUtils.SPLITER)[0];
			String cookieValue = urlAndcookieValue.split(CookieUtils.SPLITER)[1];
			this.setText("^_^ Update Cookie ("+url+")");
			this.addActionListener(new UpdateCookieWithHistory_Action(burp,burp.context,cookieValue));
		}
	}
}

class UpdateCookieWithHistory_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;
	private String cookie;

	public UpdateCookieWithHistory_Action(BurpExtender burp,IContextMenuInvocation invocation,String cookie) {
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

		String cookieValue = this.cookie;
		if (cookieValue !=null) {
			byte[] newRequestBytes = CookieUtils.updateCookie(selectedItems[0],cookieValue);

			if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
				selectedItems[0].setRequest(newRequestBytes);
			}
		}
	}
}