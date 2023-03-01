package knife;

import burp.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ProjectConfigLoadMenu extends JMenuItem {//JMenuItem vs. JMenu

    public ProjectConfigLoadMenu(BurpExtender burp){
        this.setText("^_^ Project Config Load");
        this.addActionListener(new ProjectConfigLoadMenu_Action(burp,burp.invocation));
    }
}



class ProjectConfigLoadMenu_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public ProjectConfigLoadMenu_Action(BurpExtender burp, IContextMenuInvocation invocation) {
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
    {
	   Utils.autoLoadProjectConfig(callbacks,false);
    }
}