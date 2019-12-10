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

public class DoActiveScanMenu extends JMenuItem {//JMenuItem vs. JMenu

    public DoActiveScanMenu(BurpExtender burp){
        this.setText("^_^ Do Active Scan");
        this.addActionListener(new DoActiveScan_Action(burp,burp.context));
    }
}

class DoActiveScan_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public DoActiveScan_Action(BurpExtender burp,IContextMenuInvocation invocation) {
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
        		
        		String host = message.getHttpService().getHost();
        		int port = message.getHttpService().getPort();
        		
        		boolean useHttps;
				if (message.getHttpService().getProtocol().equalsIgnoreCase("https")){
					useHttps = true;
				}else {
					useHttps = false;
				}
				byte[] request = message.getRequest();
        				
        		callbacks.doActiveScan(host, port, useHttps, request);
        	}
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}