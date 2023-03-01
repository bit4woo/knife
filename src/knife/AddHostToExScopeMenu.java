package knife;

import burp.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;

public class AddHostToExScopeMenu extends JMenuItem {//JMenuItem vs. JMenu

    public AddHostToExScopeMenu(BurpExtender burp){
        this.setText("^_^ Add Host To ExScope");
        this.addActionListener(new AddHostToExScope_Action(burp,burp.invocation));
    }
}



class AddHostToExScope_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public AddHostToExScope_Action(BurpExtender burp, IContextMenuInvocation invocation) {
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
    {
       try{
        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
        	for(IHttpRequestResponse message:messages) {
        		String url = message.getHttpService().toString();
				URL shortUrl = new URL(url);
	        	callbacks.excludeFromScope(shortUrl);
        	}
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}