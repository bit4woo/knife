package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.RobotInput;
import burp.TerminalExec;


public class DoPortScanMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public DoPortScanMenu(BurpExtender burp){
		this.setText("^_^ Run Nmap");
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
			boolean useRobot = (BurpExtender.tableModel.getConfigValueByKey("RunTerminalWithRobotInput") != null);
			if (useRobot) {
				RobotInput.startCmdConsole();//尽早启动减少出错概率
			}

			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			Set<String> hosts = new HashSet<String>();

			for(IHttpRequestResponse message:messages) {
				String host = message.getHttpService().getHost();
				hosts.add(host);
			}

			String nmapPath = burp.tableModel.getConfigValueByKey("Nmap-File-Path");
			if (nmapPath == null || nmapPath.trim().equals("")) {
				nmapPath = "nmap";
			}
			RobotInput ri = new RobotInput();
			for(String host:hosts) {
				String para = "nmap -Pn -sT -sV --min-rtt-timeout 1ms "
						+ "--max-rtt-timeout 1000ms --max-retries 0 --max-scan-delay 0 --min-rate 3000 "+host.trim();
				if (useRobot) {
					//RobotInput.startCmdConsole();
					String command = RobotInput.genCmd(null,nmapPath,para);
					ri.inputString(command);
				}else {
					TerminalExec exec = new TerminalExec(null,"nmap-knife.bat",null,nmapPath,para);
					exec.run();
				}
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(BurpExtender.getStderr());
		}
	}

	public static void main(String[] args){
	}
}
