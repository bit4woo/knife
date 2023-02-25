package knife;

import burp.*;
import config.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

public class SaveProjectConfigMenu extends JMenuItem {//JMenuItem vs. JMenu

    public SaveProjectConfigMenu(BurpExtender burp){
        this.setText("^_^ Project Config Save");
        this.addActionListener(new SaveProjectConfigMenu_Action(burp,burp.invocation));
    }
}



class SaveProjectConfigMenu_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public SaveProjectConfigMenu_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
	}

	
	@Override
	public void actionPerformed(ActionEvent e)
    {
	   String configPath  = GUI.tableModel.getConfigValueByKey("Auto_Load_Project_Config");
	   Utils.autoSaveProjectConfig(callbacks,configPath);
    }
}