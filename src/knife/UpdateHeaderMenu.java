package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.*;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IRequestInfo;


public class UpdateHeaderMenu extends JMenu {
	//JMenuItem vs. JMenu
	public BurpExtender burp;
	public IContextMenuInvocation invocation;
	public UpdateHeaderMenu(BurpExtender burp){

		this.invocation = burp.context;
		this.burp = burp;

		List<String> pHeaders = possibleHeaderNames(invocation);//HeaderNames without case change
		this.setText("^_^ Update Header");
		for (String pheader:pHeaders) {
			JMenuItem headerItem = new JMenuItem(pheader);
			headerItem.addActionListener(new UpdateHeader_Action(burp,invocation,pheader));
			this.add(headerItem);
		}
	}

	public List<String> possibleHeaderNames(IContextMenuInvocation invocation) {
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		//byte selectedInvocationContext = invocation.getInvocationContext();
		Getter getter = new Getter(burp.callbacks.getHelpers());
		HashMap<String, String> headers = getter.getHeaderHashMap(true, selectedItems[0]);

		List<String> keywords = Arrays.asList(burp.tableModel.getConfigByKey("tokenHeaders").split(","));
		List<String> ResultHeaders = new ArrayList<String>();

		Iterator<String> it = headers.keySet().iterator();
		while (it.hasNext()) {
			String item = it.next();
			if (containOneOfKeywords(item,keywords,false)) {
				ResultHeaders.add(item);
			}
		}
		return ResultHeaders;
	}

	public boolean containOneOfKeywords(String x,List<String> keywords,boolean isCaseSensitive) {
		for (String keyword:keywords) {
			if (isCaseSensitive == false) {
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
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;

	private String headerName;

	public UpdateHeader_Action(BurpExtender burp,IContextMenuInvocation invocation,String headerName) {
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
		this.headerName = headerName;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		IHttpRequestResponse messageInfo = selectedItems[0];

		String shorturl = selectedItems[0].getHttpService().toString();//current
		String urlAndtoken = CookieUtils.getLatestHeaderFromHistory(shorturl,headerName);

		if (urlAndtoken !=null) {

			Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
			String firstline = getter.getHeaderFirstLine(true,messageInfo);
			LinkedHashMap<String, String> headers = getter.getHeaderHashMap(true,messageInfo);
			byte[] body = getter.getBody(true,messageInfo);

			headers.put(headerName,urlAndtoken.split(CookieUtils.SPLITER)[1]);
			List<String> headerList = getter.HeaderMapToList(firstline,headers);

			byte[] newRequestBytes = BurpExtender.callbacks.getHelpers().buildHttpMessage(headerList, body);
			selectedItems[0].setRequest(newRequestBytes);
		}
	}

}
