package knife;

import burp.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class DismissHostMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DismissHostMenu(BurpExtender burp){
		String dismissed  = burp.tableModel.getConfigValueByKey("DismissedHost");
		if (dismissed != null) {
			this.setText("^_^ Dismissed This Host");
			this.addActionListener(new Dismiss_Host_Action(burp,burp.invocation));
		}
	}
}

class Dismiss_Host_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public Dismiss_Host_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.invocation  = invocation;
		this.myburp = burp;
        this.helpers = burp.helpers;
        this.callbacks = BurpExtender.callbacks;
        this.stderr = burp.stderr;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		try{
			Set<String> dissmissed  = myburp.tableModel.getConfigValueSetByKey("DismissedHost");

			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			for(IHttpRequestResponse message:messages) {
				String host = message.getHttpService().getHost();
				dissmissed.add(host);
			}
			myburp.tableModel.setConfigValueSetByKey("DismissedHost",dissmissed);
		}catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}