package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;

public class DoActiveScanMenu extends JMenuItem {//JMenuItem vs. JMenu

	public DoActiveScanMenu(BurpExtender burp){
		try {
			final String majorVersion = BurpExtender.callbacks.getBurpVersion()[1].replaceAll("[a-zA-Z]","");
			final String minorVersion = BurpExtender.callbacks.getBurpVersion()[2].replaceAll("[a-zA-Z]","");//18beta
			//stdout.println(majorVersion+"   "+minorVersion);
			//2020.2.1 ==>2020   2.1
			//2.1.06 ==> 2.1   06
			float majorV = Float.parseFloat(majorVersion);
			float minorV = Float.parseFloat(minorVersion);
			if (majorV>=2020 && minorV >= 2.0f) { //2020.2及之后

			}else if (majorV < 2) {//1点几版本不需要

			}else {
				this.setText("^_^ Do Active Scan");
				this.addActionListener(new DoActiveScan_Action(burp,burp.invocation));
			}
			//2020.2版本之后续版本添加了主动扫描选项
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
			this.setText("^_^ Do Active Scan");
			this.addActionListener(new DoActiveScan_Action(burp,burp.invocation));
		}
	}
}

class DoActiveScan_Action implements ActionListener{
	//scope matching is actually String matching!!
	private IContextMenuInvocation invocation;
	public BurpExtender myburp;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	//callbacks.printOutput(Integer.toString(invocation.getToolFlag()));//issue tab of target map is 16
	public DoActiveScan_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
	}


	@Override
	public void actionPerformed(ActionEvent e)
	{
		try{
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();
			for(IHttpRequestResponse message:messages) {

				String host = message.getHttpService().getHost();
				int port = message.getHttpService().getPort();

				boolean useHttps;
				if (message.getHttpService().getProtocol().equalsIgnoreCase("https")){
					useHttps = true;
				}else {
					useHttps = false;
				}
				byte[] request = message.getRequest();

				callbacks.doActiveScan(host, port, useHttps, request);
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}