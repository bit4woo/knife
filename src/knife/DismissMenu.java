package knife;

import burp.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DismissMenu extends JMenuItem {//JMenuItem vs. JMenu

    public DismissMenu(BurpExtender burp){
        this.setText("^_^ Dismiss");
        this.addActionListener(new Dismiss_Action(burp,burp.context));
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
		   String dissmissed  = myburp.tableModel.getConfigByKey("DismissedHost");
		   String[] dissmissedHosts = dissmissed.split(",");
		   List<String> dissmissedHostList = Arrays.asList(dissmissedHosts);
		   dissmissedHostList = new ArrayList<>(dissmissedHostList);

        	IHttpRequestResponse[] messages = invocation.getSelectedMessages();
        	for(IHttpRequestResponse message:messages) {
        		String host = message.getHttpService().getHost();
        		if (!myburp.isDismissedHost(host)){
					dissmissedHostList.add(host);
					//https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception/5755510
				}
        	}

        	myburp.tableModel.setConfigByKey("DismissedHost",Arrays.toString(dissmissedHostList.toArray()).replace("[","").replace("]",""));
        }catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}