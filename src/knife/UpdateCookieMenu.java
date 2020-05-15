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

public class UpdateCookieMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public UpdateCookieMenu(BurpExtender burp){
		if (burp.invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
			this.setText("^_^ Update Cookie");
			this.addActionListener(new UpdateCookieAction(burp,burp.invocation));
		}
	}
}

class UpdateCookieAction implements ActionListener {
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public UpdateCookieAction(BurpExtender burp, IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();

		Getter getter = new Getter(helpers);
		String sourceshorturl = getter.getShortURL(selectedItems[0]).toString();
		HeaderEntry latestCookie = CookieUtils.getLatestCookieFromHistory(sourceshorturl);//自行查找一次

		//通过弹窗交互 获取Cookie
		int time = 0;
		while (!isVaildCookie(latestCookie) && time <2) {
			latestCookie = CookieUtils.getLatestCookieFromSpeicified();
			time++;
		}

		if (isVaildCookie(latestCookie)) {
			String latestCookieValue = latestCookie.getHeaderValue();
			sourceshorturl = latestCookie.getHeaderSource();

			byte[] newRequest = CookieUtils.updateCookie(selectedItems[0], latestCookieValue);
			selectedItems[0].setRequest(newRequest);

			if (sourceshorturl.startsWith("http")) {
				this.burp.config.setUsedCookie(latestCookie);
			}
		}else {
			//do nothing
		}
	}

	public boolean isVaildCookie(HeaderEntry urlAndCookieString) {
		if (urlAndCookieString == null) {
			return false;
		}
		String currentCookie = new Getter(helpers).getHeaderValueOf(true,invocation.getSelectedMessages()[0],"Cookie");
		String foundCookie = urlAndCookieString.getHeaderValue();
		if (foundCookie.equalsIgnoreCase(currentCookie)) {
			return false;
		}
		return true;
	}
}