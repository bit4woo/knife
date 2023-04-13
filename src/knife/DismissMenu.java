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
import config.DismissedTargetsManager;

public class DismissMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DismissMenu(BurpExtender burp){
		String dismissed  = burp.tableModel.getConfigValueByKey("DismissedTargets");
		if (dismissed != null) {
			this.setText("^_^ Dismissed");
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
		int action = fetchChangeType();

		IHttpRequestResponse[] messages = invocation.getSelectedMessages();
		if (action == 1) {
			DismissedTargetsManager.putRule(messages, DismissedTargetsManager.ACTION_DROP_HOST);
		}else if(action == 2) {
			DismissedTargetsManager.putRule(messages, DismissedTargetsManager.ACTION_DROP_URL);
		}else if(action == 3) {
			DismissedTargetsManager.putRule(messages, DismissedTargetsManager.ACTION_Forward_HOST);
		}else if(action == 4) {
			DismissedTargetsManager.putRule(messages, DismissedTargetsManager.ACTION_Forward_URL);
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