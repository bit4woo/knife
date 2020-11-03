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

public class DismissCancelMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DismissCancelMenu(BurpExtender burp){
		this.setText("^_^ Cancle Dismissed");
		this.addActionListener(new Dismiss_URL_Action(burp,burp.invocation));
	}
}


class Dismiss_Cancel_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
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
		this.stderr = burp.stderr;
	}


	@Override
	public void actionPerformed(ActionEvent e)
	{
		try{
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			for(IHttpRequestResponse message:messages) {
				Getter getter = new Getter(helpers);
				String url = getter.getFullURL(message).toString();
				String host = getter.getHost(message);
				if (url.contains("?")){
					url = url.substring(0,url.indexOf("?"));
				}
				System.out.println(url);
				stderr.println(url);
				if (myburp.isDismissedURL(url)){
					stderr.println("is dismissed url");
					Set<String> dismissed  = myburp.tableModel.getConfigValueSetByKey("DismissedURL");
					dismissed.remove(url);
					myburp.tableModel.setConfigValueSetByKey("DismissedURL",dismissed);
				}
				if (myburp.isDismissedHost(host)){
					Set<String> dismissed  = myburp.tableModel.getConfigValueSetByKey("DismissedHost");
					dismissed.remove(host);
					myburp.tableModel.setConfigValueSetByKey("DismissedHost",dismissed);
				}
			}
		}catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}