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

public class SetCookieMenu extends JMenuItem {
	//JMenuItem vs. JMenu

	private static final long serialVersionUID = 1L;

	public SetCookieMenu(BurpExtender burp){
		this.setText("^_^ Set Cookie");
		this.addActionListener(new SetCookie_Action(burp,burp.invocation));
	}
}

class SetCookie_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public SetCookie_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try{
			//stdout.println("SetCookie_Action called");
			String cookieEntry = HeaderManager.getLatestCookieFromUserInput();

			if (cookieEntry != null) {//当没有找到相应的cookie时为null
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();
				if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					HeaderManager.updateCookie(true,messages[0], cookieEntry);
				}

				HeaderManager.addHandleRule(messages,cookieEntry);
				HeaderManager.setUsedCookieOfUpdate(cookieEntry);
			}else {
				stderr.println("No cookie found with your input");
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}
