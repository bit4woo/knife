package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import config.ProcessManager;

public class UpdateCookieWithHistoryMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public UpdateCookieWithHistoryMenu(BurpExtender burp){

		try {
			if (burp.invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST
					|| burp.invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_INTRUDER_PAYLOAD_POSITIONS) {
				String usedCookie = ProcessManager.getUsedCookieOfUpdate();
				if (usedCookie != null) {
					this.setText("^_^ Update Cookie ("+ ProcessManager.fetchUsedCookieAsTips()+")");
					this.addActionListener(new UpdateCookieWithHistory_Action(burp,burp.invocation,usedCookie));
				}
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
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
			if(selectedInvocationContext == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
				ProcessManager.updateCookie(true,selectedItems[0],cookieValue);
			}
		}
	}
}