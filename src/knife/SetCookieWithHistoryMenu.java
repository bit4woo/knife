package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.*;
import javax.swing.JMenuItem;

import burp.*;

public class SetCookieWithHistoryMenu extends JMenuItem {
	//JMenuItem vs. JMenu

	private static final long serialVersionUID = 1L;

	public SetCookieWithHistoryMenu(BurpExtender burp){
		if (burp.config.getTmpMap().containsKey("cookieToSetHistory")) {
			String cookieToSetHistory = burp.config.getTmpMap().get("cookieToSetHistory");

			String targetUrl = cookieToSetHistory.split(CookieUtils.SPLITER)[0];
			String originUrl = cookieToSetHistory.split(CookieUtils.SPLITER)[1];
			String cookieValue = cookieToSetHistory.split(CookieUtils.SPLITER)[2];

			this.setText(String.format("^_^ Set Cookie (%s)",originUrl));
			this.addActionListener(new SetCookieWithHistory_Action(burp,burp.context));
		}

	}
}

class SetCookieWithHistory_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public SetCookieWithHistory_Action(BurpExtender burp,IContextMenuInvocation invocation) {
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
		String shortUrl = getter.getShortUrl(selectedItems[0]);

		String cookieToSetHistory = burp.config.getTmpMap().get("cookieToSetHistory");
		String targetUrl = cookieToSetHistory.split(CookieUtils.SPLITER)[0];
		String originUrl = cookieToSetHistory.split(CookieUtils.SPLITER)[1];
		String cookieValue = cookieToSetHistory.split(CookieUtils.SPLITER)[2];

//		if (shortUrl.equalsIgnoreCase(targetUrl)){//update request, processProxyMessage will deal response.
//			byte[] newRequest = CookieUtils.updateCookie(selectedItems[0],cookieValue);
//			selectedItems[0].setRequest(newRequest);
//		}

		this.burp.config.getTmpMap().put("cookieToSet", shortUrl+CookieUtils.SPLITER+originUrl+CookieUtils.SPLITER+cookieValue);
		//这个设置，让proxy处理它的响应包，shortUrl是新的target
	}
}
