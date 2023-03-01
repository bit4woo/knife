package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.HashSet;

import javax.swing.JMenuItem;

import burp.*;
import config.GUI;

public class AddHostToInScopeAdvMenu extends JMenuItem {//JMenuItem vs. JMenu

    public AddHostToInScopeAdvMenu(BurpExtender burp){
        this.setText("^_^ Add Host To InScope Adv");
        this.addActionListener(new AddHostToInScopeAdv_Action(burp,burp.invocation));
    }
}



class AddHostToInScopeAdv_Action implements ActionListener{
    //scope matching is actually String matching!!
    private IContextMenuInvocation invocation;
    public BurpExtender myburp;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public IBurpExtenderCallbacks callbacks;
    //callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
    public AddHostToInScopeAdv_Action(BurpExtender burp, IContextMenuInvocation invocation) {
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
            Utils.AddHostToInScopeAdvByProjectConfig(callbacks,hostHashSet);
        }
        catch (Exception e1)
        {
            e1.printStackTrace(stderr);
        }
    }
}