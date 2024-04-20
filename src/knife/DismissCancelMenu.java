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

public class DismissCancelMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DismissCancelMenu(BurpExtender burp){
		this.setText("^_^ Dismiss Cancle");
		this.addActionListener(new Dismiss_Cancel_Action(burp,burp.invocation));
	}
}


class Dismiss_Cancel_Action implements ActionListener{
	//scope matching is actually String matching!!
	private final IContextMenuInvocation invocation;
	public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public Dismiss_Cancel_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.invocation  = invocation;
		this.myburp = burp;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
	}


	@Override
	public void actionPerformed(ActionEvent e)
	{
		ProcessManager.removeDismissRule(invocation.getToolFlag(),invocation.getSelectedMessages());
	}
}