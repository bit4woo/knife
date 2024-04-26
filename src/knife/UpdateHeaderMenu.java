package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.bit4woo.utilbox.burp.HelperPlus;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import config.GUI;
import config.ProcessManager;


public class UpdateHeaderMenu extends JMenu {
	//JMenuItem vs. JMenu
	//JMenu 可以有子菜单，即使具体实现为空，也会显示
	//JMenuItem 不可以有子菜单，当实现为空的时候，是不会显示的
	public BurpExtender burp;
	public IContextMenuInvocation invocation;
	public UpdateHeaderMenu(BurpExtender burp){

		try {
			this.invocation = burp.invocation;
			this.burp = burp;

			if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {

				List<String> pHeaders = possibleHeaderNames(invocation);//HeaderNames without case change
				/*menu_list.add(uhmenu);*/
				if(!pHeaders.isEmpty()) {
					this.setText("^_^ Update Header");
					for (String pheader:pHeaders) {
						JMenuItem headerItem = new JMenuItem(pheader);
						headerItem.addActionListener(new UpdateHeader_Action(burp,invocation,pheader));
						this.add(headerItem);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
	}

	public List<String> possibleHeaderNames(IContextMenuInvocation invocation) {
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		//byte selectedInvocationContext = invocation.getInvocationContext();
		HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
		List<String> headers = getter.getHeaderList(true, selectedItems[0]);

		String tokenHeadersStr = GUI.getConfigTableModel().getConfigValueByKey("tokenHeaders");

		List<String> ResultHeaders = new ArrayList<String>();
		
		if (tokenHeadersStr!= null && headers != null) {
			String[] tokenHeaders = tokenHeadersStr.split(",");
			List<String> keywords = Arrays.asList(tokenHeaders);
			for (String header:headers) {
				try {
					String headerKey = header.split(":",2)[0].trim();
					if (containOneOfKeywords(headerKey,keywords,false)) {
						ResultHeaders.add(headerKey);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return ResultHeaders;
	}

	public boolean containOneOfKeywords(String x,List<String> keywords,boolean isCaseSensitive) {
		for (String keyword:keywords) {
			if (!isCaseSensitive) {
				x = x.toLowerCase();
				keyword = keyword.toLowerCase();
			}
			if (x.contains(keyword)){
				return true;
			}
		}
		return false;
	}
}

class UpdateHeader_Action implements ActionListener{
	private final IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;

	private final String headerName;

	public UpdateHeader_Action(BurpExtender burp,IContextMenuInvocation invocation,String headerName) {
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
		this.stdout = BurpExtender.stdout;
		this.headerName = headerName;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
			IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
			String headerLine = ProcessManager.getLatestHeaderFromHistory(selectedItems[0], headerName);

			if (headerLine != null) {
				ProcessManager.updateHeader(true,selectedItems[0],headerLine);
			}
		}
	}

}
