package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.swing.JMenuItem;

import burp.*;

public class AddHostToScopeAdvMenu extends JMenuItem {//JMenuItem vs. JMenu

    public AddHostToScopeAdvMenu(BurpExtender burp){
        this.setText("^_^ Add Host To Scope Adv");
        this.addActionListener(new AddHostToScopeAdv_Action(burp,burp.invocation));
    }
}



class AddHostToScopeAdv_Action implements ActionListener{
    //scope matching is actually String matching!!
    private IContextMenuInvocation invocation;
    public BurpExtender myburp;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public IBurpExtenderCallbacks callbacks;
    //callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
    public AddHostToScopeAdv_Action(BurpExtender burp,IContextMenuInvocation invocation) {
        this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        try{
            HashSet<String> hostHashSet = new HashSet<>();
            IHttpRequestResponse[] messages = invocation.getSelectedMessages();
            for(IHttpRequestResponse message:messages) {
                String host = message.getHttpService().getHost();
                hostHashSet.add(host);
            }
            Utils.AddHostToScopeAdvByProjectConfig(callbacks,hostHashSet);
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}