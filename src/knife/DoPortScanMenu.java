package knife;

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
import burp.Utils;


public class DoPortScanMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public DoPortScanMenu(BurpExtender burp){
		this.setText("^_^ Do Port Scan");
		this.addActionListener(new DoPortScan_Action(burp,burp.context));
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
        	
        	for(String host:hosts) {
    			String batFilePathString  = genbatFile(host);
    			String command = NmapScanCommand(batFilePathString);
    			Process process = Runtime.getRuntime().exec(command);
			}

		}
		catch (Exception e1)
		{
			e1.printStackTrace(BurpExtender.getStderr());
		}
	}
	
	public String genbatFile(String host) {
		try {
			String basedir = (String) System.getProperties().get("java.io.tmpdir");
			String nmapPath = burp.tableModel.getConfigValueByKey("Nmap-File-Path");
			if (nmapPath ==null || nmapPath.trim().equals("")) {
				nmapPath = "nmap";
			}else if (nmapPath.contains(" ")) {//如果路径中包含空格，需要引号
				nmapPath = "\""+nmapPath+"\"";
			}
			
			String command = nmapPath+" -v -A -p 1-65535 "+host.trim();

			//将命令写入剪切板
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(command.toString());
			clipboard.setContents(selection, null);
			
			File batFile = new File(basedir,"Nmap-latest-command.bat");
			if (!batFile.exists()) {
			    batFile.createNewFile();
			}
			
			FileUtils.writeByteArrayToFile(batFile, command.toString().getBytes());
			return batFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace(BurpExtender.getStderr());
			return null;
		}
	}
	
	public static String NmapScanCommand(String batfilepath) {
		String command = "";
		if (Utils.isWindows()) {
			command="cmd /c start " + batfilepath;
		} else {
			if (new File("/bin/sh").exists()) {
				command="/bin/sh " + batfilepath;
			}
			else if (new File("/bin/bash").exists()) {
				command="/bin/bash " + batfilepath;
			}
		}
		return command;
	}
	
	public static void main(String[] args){
	}
}
