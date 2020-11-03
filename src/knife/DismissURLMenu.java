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

public class DismissURLMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DismissURLMenu(BurpExtender burp){
		String dismissed  = burp.tableModel.getConfigValueByKey("DismissedURL");
		if (dismissed != null) {
			this.setText("^_^ Dismissed this url");
			this.addActionListener(new Dismiss_URL_Action(burp,burp.invocation));
		}
	}
}


class Dismiss_URL_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
	public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public Dismiss_URL_Action(BurpExtender burp,IContextMenuInvocation invocation) {
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
			Set<String> dissmissed  = myburp.tableModel.getConfigValueSetByKey("DismissedURL");

			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			for(IHttpRequestResponse message:messages) {
				Getter getter = new Getter(helpers);
				String url = getter.getFullURL(message).toString();
				if (url.contains("?")){
					url = url.substring(0,url.indexOf("?"));
				}
				dissmissed.add(url);
			}
			myburp.tableModel.setConfigValueSetByKey("DismissedURL",dissmissed);
		}catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}