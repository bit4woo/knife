package knife;

import burp.*;
import config.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.HashSet;

public class AddHostToExScopeAdvMenu extends JMenuItem {//JMenuItem vs. JMenu

    public AddHostToExScopeAdvMenu(BurpExtender burp){
        this.setText("^_^ Add Host To ExScope Adv");
        this.addActionListener(new AddHostToExScopeAdv_Action(burp,burp.invocation));
    }
}



class AddHostToExScopeAdv_Action implements ActionListener{
    //scope matching is actually String matching!!
    private IContextMenuInvocation invocation;
    public BurpExtender myburp;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public IBurpExtenderCallbacks callbacks;
    //callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
    public AddHostToExScopeAdv_Action(BurpExtender burp, IContextMenuInvocation invocation) {
        this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        try{
            String wildcardSet  = GUI.tableModel.getConfigValueByKey("Scope_Set_Base_On_Wildcard_SubDomain");
            HashSet<String> hostHashSet = new HashSet<>();
            IHttpRequestResponse[] messages = invocation.getSelectedMessages();
            for(IHttpRequestResponse message:messages) {
                String host = message.getHttpService().getHost();
                if(wildcardSet!=null){
                    host = Utils.hostToWildcardHostWithDotEscape(host);
                }else {
                    host = Utils.dotToEscapeDot(host);
                }
                hostHashSet.add(host);
            }
            Utils.AddHostToExScopeAdvByProjectConfig(callbacks,hostHashSet);
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}