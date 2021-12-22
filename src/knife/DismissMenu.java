package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.Utils;
import config.DismissedTargets;

public class DismissMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DismissMenu(BurpExtender burp){
		String dismissed  = burp.tableModel.getConfigValueByKey("DismissedTargets");
		if (dismissed != null) {
			this.setText("^_^ Dismiss");
			this.addActionListener(new Dismiss_Action(burp,burp.invocation));
		}
	}
}


class Dismiss_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
	public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public Dismiss_Action(BurpExtender burp,IContextMenuInvocation invocation) {
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
			DismissedTargets.FromGUI();
			
			int action = fetchChangeType();

			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			for(IHttpRequestResponse message:messages) {
				String host = message.getHttpService().getHost();
				String url = new HelperPlus(helpers).getFullURL(message).toString();
				if (url.contains("?")){
					url = url.substring(0,url.indexOf("?"));
				}
				
				if (action == 1) {
					DismissedTargets.targets.put(host, DismissedTargets.ACTION_DROP);
				}else if(action == 2) {
					DismissedTargets.targets.put(url, DismissedTargets.ACTION_DROP);
				}else if(action == 3) {
					DismissedTargets.targets.put(host, DismissedTargets.ACTION_DONT_INTERCEPT);
				}else if(action == 4) {
					DismissedTargets.targets.put(url, DismissedTargets.ACTION_DONT_INTERCEPT);
				}
			}
			DismissedTargets.ShowToGUI();
		}catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
	
	public static int fetchChangeType() {
		Object[] options = { "Help","Drop Host","Drop URL","Forward Host","Forward URL"};
		int user_input = JOptionPane.showOptionDialog(null, "Which Action Do You Want To Take?", "Chose Your Action And Scope",
		JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
		null, options, options[0]);
		if (user_input ==0) {
			try {
				Utils.browserOpen("https://github.com/bit4woo/knife/blob/master/Help.md", null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			user_input = fetchChangeType();
		}
		return user_input;
	}
}