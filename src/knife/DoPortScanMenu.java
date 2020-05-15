package knife;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;

import org.apache.commons.io.FileUtils;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.RobotInput;
import burp.Utils;


public class DoPortScanMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public DoPortScanMenu(BurpExtender burp){
		this.setText("^_^ Do Port Scan");
		this.addActionListener(new DoPortScan_Action(burp,burp.invocation));
	}
}

class DoPortScan_Action implements ActionListener{

	private IContextMenuInvocation invocation;
    public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public DoPortScan_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.invocation  = invocation;
		this.burp = burp;
        this.helpers = burp.helpers;
        this.callbacks = burp.callbacks;
        this.stderr = burp.stderr;
	}

	
	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		try{
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			Set<String> hosts = new HashSet<String>();
			
        	for(IHttpRequestResponse message:messages) {
        		String host = message.getHttpService().getHost();
        		hosts.add(host);
			}
        	
        	RobotInput ri = new RobotInput();
        	for(String host:hosts) {
        		RobotInput.startCmdConsole();
				String command = genNmapCmd(host);
				ri.inputString(command);

			}

		}
		catch (Exception e1)
		{
			e1.printStackTrace(BurpExtender.getStderr());
		}
	}
	
	public String genNmapCmd(String host) {
			String nmapPath = burp.tableModel.getConfigValueByKey("Nmap-File-Path");
			if (nmapPath ==null || nmapPath.trim().equals("")) {
				nmapPath = "nmap";
			}else if (nmapPath.contains(" ")) {//如果路径中包含空格，需要引号
				nmapPath = "\""+nmapPath+"\"";
			}
			
			String command = nmapPath+" -v -A -p 1-65535 "+host.trim()+System.lineSeparator();
			return command;
	}
	

	
	public static void main(String[] args){
	}
}
