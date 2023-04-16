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
import manager.HeaderManager;

public class SetCookieWithHistoryMenu extends JMenuItem {
	//JMenuItem vs. JMenu

	private static final long serialVersionUID = 1L;

	public SetCookieWithHistoryMenu(BurpExtender burp){
		try {
			String cookieToSetHistory = HeaderManager.getUsedCookieOfUpdate();
			if (cookieToSetHistory != null) {
				this.setText(String.format("^_^ Set Cookie (%s)",HeaderManager.fetchUsedCookieAsTips()));
				this.addActionListener(new SetCookieWithHistory_Action(burp,burp.invocation));
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
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

		String cookieToSetHistory = HeaderManager.getUsedCookieOfUpdate();
		if (cookieToSetHistory != null) {
			try {
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();
				HeaderManager.addHandleRule(messages,cookieToSetHistory);
			} catch (Exception e1) {
				e1.printStackTrace(stderr);
			}
		}
	}
}
