package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;

public class AddHostToScopeMenu extends JMenuItem {//JMenuItem vs. JMenu

    public AddHostToScopeMenu(BurpExtender burp){
        this.setText("^_^ Add Host To Scope");
        this.addActionListener(new AddHostToScope_Action(burp,burp.invocation));
    }
}



class AddHostToScope_Action implements ActionListener{
	//scope matching is actually String matching!!
	private final IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public AddHostToScope_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = BurpExtender.callbacks;
        this.stderr = BurpExtender.stderr;
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
    {
       try{
        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
        	for(IHttpRequestResponse message:messages) {
        		String url = message.getHttpService().toString();
				URL shortUrl = new URL(url);
	        	callbacks.includeInScope(shortUrl);
        	}
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}