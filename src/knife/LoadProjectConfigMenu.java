package knife;

import burp.*;
import config.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class LoadProjectConfigMenu extends JMenuItem {//JMenuItem vs. JMenu

    public LoadProjectConfigMenu(BurpExtender burp){
        this.setText("^_^ Project Config Load");
        this.addActionListener(new LoadProjectConfigMenu_Action(burp,burp.invocation));
    }
}



class LoadProjectConfigMenu_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public LoadProjectConfigMenu_Action(BurpExtender burp, IContextMenuInvocation invocation) {
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