package Deprecated;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;

public class DismissAutoForwardMenu extends JMenu {//JMenuItem vs. JMenu

	public DismissAutoForwardMenu(BurpExtender burp){
		String dismissed  = burp.tableModel.getConfigValueByKey("DismissedAutoForward");
		if (dismissed != null) {
			this.setText("^_^ Dismissed This Url");
			
			JMenuItem item = new JMenuItem("Selected Host");
			item.addActionListener(new Dismiss_Auto_Forward_Action(burp,burp.invocation));
			this.add(item);
			
			JMenuItem item1 = new JMenuItem("Selected URL");
			item1.addActionListener(new Dismiss_Auto_Forward_Action(burp,burp.invocation));
			this.add(item1);
		}
	}
}


class Dismiss_Auto_Forward_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
	public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public Dismiss_Auto_Forward_Action(BurpExtender burp,IContextMenuInvocation invocation) {
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
			Set<String> dissmissed  = myburp.tableModel.getConfigValueSetByKey("DismissedAutoForward");

			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			
			if (e.getActionCommand().equalsIgnoreCase("This Host")) {
				for(IHttpRequestResponse message:messages) {
					String host = message.getHttpService().getHost();
					dissmissed.add(host);
				}
			}else {
				for(IHttpRequestResponse message:messages) {
					Getter getter = new Getter(helpers);
					String url = getter.getFullURL(message).toString();
					if (url.contains("?")){
						url = url.substring(0,url.indexOf("?"));
					}
					dissmissed.add(url);
				}
			}
			
			myburp.tableModel.setConfigValueSetByKey("DismissedAutoForward",dissmissed);
		}catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}