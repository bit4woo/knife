package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;

import burp.*;

public class ProjectScopeClearMenu extends JMenuItem {//JMenuItem vs. JMenu

    public ProjectScopeClearMenu(BurpExtender burp){
        this.setText("^_^ Project Scope Clear");
        this.addActionListener(new ProjectScopeClear_Action(burp,burp.invocation));
    }
}



class ProjectScopeClear_Action implements ActionListener{
    //scope matching is actually String matching!!
    private IContextMenuInvocation invocation;
    public BurpExtender myburp;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public IBurpExtenderCallbacks callbacks;
    //callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
    public ProjectScopeClear_Action(BurpExtender burp, IContextMenuInvocation invocation) {
        this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        try{
            Utils.ClearAllScopeAdvByProjectConfig(callbacks);
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}