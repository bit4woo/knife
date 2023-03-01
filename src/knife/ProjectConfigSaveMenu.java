package knife;

import burp.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class ProjectConfigSaveMenu extends JMenuItem {//JMenuItem vs. JMenu

    public ProjectConfigSaveMenu(BurpExtender burp){
        this.setText("^_^ Project Config Save");
        this.addActionListener(new ProjectConfigSaveMenu_Action(burp,burp.invocation));
    }
}



class ProjectConfigSaveMenu_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public ProjectConfigSaveMenu_Action(BurpExtender burp, IContextMenuInvocation invocation) {
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
    {
	   Utils.autoSaveProjectConfig(callbacks);
    }
}