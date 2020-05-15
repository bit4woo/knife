package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;

public class SetCookieWithHistoryMenu extends JMenuItem {
	//JMenuItem vs. JMenu

	private static final long serialVersionUID = 1L;

	public SetCookieWithHistoryMenu(BurpExtender burp){
		HeaderEntry cookieToSetHistory = burp.config.getUsedCookie();
		if (cookieToSetHistory != null) {
			String targetUrl = cookieToSetHistory.getTargetUrl();
			String originUrl = cookieToSetHistory.getHeaderSource();
			String cookieValue = cookieToSetHistory.getHeaderValue();

			this.setText(String.format("^_^ Set Cookie (%s)",originUrl));
			this.addActionListener(new SetCookieWithHistory_Action(burp,burp.invocation));
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

		HeaderEntry cookieToSetHistory = burp.config.getUsedCookie();

		//		if (shortUrl.equalsIgnoreCase(targetUrl)){//update request, processProxyMessage will deal response.
		//			byte[] newRequest = CookieUtils.updateCookie(selectedItems[0],cookieValue);
		//			selectedItems[0].setRequest(newRequest);
		//		}

		try{
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			for(IHttpRequestResponse message:messages) {
				Getter getter = new Getter(helpers);
				String targetShortUrl = getter.getShortURL(message).toString();
				cookieToSetHistory.setTargetUrl(targetShortUrl);
				this.burp.config.getSetCookieMap().put(targetShortUrl, cookieToSetHistory);
				//这个设置，让proxy处理它的响应包，shortUrl是新的target
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}
