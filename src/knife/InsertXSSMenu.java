package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.List;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;

public class InsertXSSMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public InsertXSSMenu(BurpExtender burp){
		this.setText("^_^ Insert XSS");
		this.addActionListener(new InsertXSSAction(burp,burp.context));
	}
}

class InsertXSSAction implements ActionListener {
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public InsertXSSAction(BurpExtender burp, IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		
		IHttpRequestResponse messageInfo = selectedItems[0];
		byte[] newRequest = messageInfo.getRequest();
		
		Getter getter = new Getter(helpers);
		List<IParameter> paras = getter.getParas(messageInfo);
		String xsspayload = burp.tableModel.getConfigByKey("XSS-Payload");
		
		if (xsspayload == null) return;
		
		for(IParameter para:paras) {
			String value = para.getValue();
			if (isInt(value)) {
				continue;
			}else {
				value = value+xsspayload;
				IParameter newPara = helpers.buildParameter(para.getName(), value, para.getType());
				newRequest = helpers.updateParameter(newRequest, newPara);
			}
		}
		messageInfo.setRequest(newRequest);
	}
	
	public static boolean isInt(String input) {
		try {
			Integer b = Integer.valueOf(input);
			return true;
		} catch (NumberFormatException e) {
			try {
				long l = Long.valueOf(input);
				return true;
			}catch(Exception e1) {
				
			}
			return false;
		}
	}
	
	public static void main(String[] args) {
		System.out.println(isInt("13175192849"));
	}
}