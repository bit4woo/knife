package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.GUI;


public class ChunkedEncodingMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public BurpExtender burp;
	public IContextMenuInvocation invocation;
	public Getter getter;
    public ChunkedEncodingMenu(BurpExtender burp){
    	
    	try {
			this.invocation = burp.invocation;
			this.burp = burp;
			this.getter = new Getter(burp.helpers);
			IHttpRequestResponse[] messages = this.invocation.getSelectedMessages();
			if (messages == null || messages.length == 0) {
				return;
			}
			String chunked = getter.getHeaderValueOf(true, messages[0], "Transfer-Encoding");
			if (chunked == null || !chunked.equalsIgnoreCase("chunked") ) {
				this.setText("^_^ Chunked Encoding");
			}else {
				this.setText("^_^ Chunked Decoding");
			}
			this.addActionListener(new ChunkedEncoding_Action(burp,invocation));
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
    }
}

class ChunkedEncoding_Action implements ActionListener{
	private final IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	private final BurpExtender burp;
	
	public ChunkedEncoding_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
        this.helpers = burp.helpers;
        this.callbacks = BurpExtender.callbacks;
        this.stderr = BurpExtender.stderr;
        this.stdout = BurpExtender.stdout;
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		IHttpRequestResponse messageInfo = selectedItems[0];
		
		Getter getter = new Getter(helpers);
		
    	List<String> headers = getter.getHeaderList(true, messageInfo);
    	byte[] body = getter.getBody(true, messageInfo);
    	
    	if (event.getActionCommand().equals("^_^ Chunked Encoding")) {
            Iterator<String> iter = headers.iterator();
            while (iter.hasNext()) {
                if (((String)iter.next()).contains("Transfer-Encoding")) {
                    iter.remove();
                }
            }
            headers.add("Transfer-Encoding: chunked");
        	
            try {
            	boolean useComment =false;
            	if (GUI.tableModel.getConfigValueByKey("Chunked-UseComment") != null) {
            		useComment = true;
            	}
            	String lenStr = GUI.tableModel.getConfigValueByKey("Chunked-Length");
            	int len =10;
            	if (lenStr !=null) {
            		len = Integer.parseInt(lenStr);
            	}
    			body = Methods.encoding(body,len,useComment);
    		} catch (UnsupportedEncodingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}else if (event.getActionCommand().equals("^_^ Chunked Decoding")) {
            Iterator<String> iter = headers.iterator();
            while (iter.hasNext()) {
                if (((String)iter.next()).contains("Transfer-Encoding")) {
                    iter.remove();
                }
            }
        	
            try {
    			body = Methods.decoding(body);
    		} catch (UnsupportedEncodingException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}

    	byte[] newRequestBytes = helpers.buildHttpMessage(headers, body);
    	
    	selectedItems[0].setRequest(newRequestBytes);
	}
	
	

}
