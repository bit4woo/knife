package knife;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;

import com.bit4woo.utilbox.burp.HelperPlus;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;


public class ViewChineseMenu extends JMenuItem {
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public ViewChineseMenu(BurpExtender burp){
		this.setText("^_^ View Chinese");
		this.addActionListener(new View_Action(burp,burp.invocation));
	}
}

class View_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public View_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
		this.stdout = BurpExtender.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		IHttpRequestResponse[] messages = invocation.getSelectedMessages();
		HelperPlus getter = new HelperPlus(helpers);
		if (messages == null) {
			return;
		}
		if (messages.length == 1) {
			byte[] respBody = getter.getBody(false, messages[0]);

			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						ChineseGUI GUI = new ChineseGUI(respBody);
						GUI.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}
}
