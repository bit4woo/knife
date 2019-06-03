package knife;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import config.ConfigEntry;

public class OpenWithBrowserMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public OpenWithBrowserMenu(BurpExtender burp){
		this.setText("^_^ Open with browser");
		this.addActionListener(new OpenWithBrowser_Action(burp,burp.context));
	}
}

class OpenWithBrowser_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public OpenWithBrowser_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try{
			IHttpRequestResponse[] messages = invocation.getSelectedMessages();

			if (messages !=null) {
				IHttpRequestResponse message = messages[0];

				String browserPath = burp.tableModel.getConfigByKey("browserPath");
				if (browserPath!=null && new File(browserPath).exists() && new File(browserPath).isFile()) {

				}else {//when no browserPath in config, the value will be null
					browserPath = "default";
				}

				/////////////selected url/////////////////
				byte[] source = null;

				String hosturl =helpers.analyzeRequest(message).getUrl().toString();
				int context = invocation.getInvocationContext();
				if (context==IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST
						|| context ==IContextMenuInvocation.CONTEXT_MESSAGE_VIEWER_REQUEST
						|| context == IContextMenuInvocation.CONTEXT_PROXY_HISTORY
						|| context == IContextMenuInvocation.CONTEXT_INTRUDER_ATTACK_RESULTS
						|| context == IContextMenuInvocation.CONTEXT_SEARCH_RESULTS
						|| context == IContextMenuInvocation.CONTEXT_TARGET_SITE_MAP_TABLE
						|| context == IContextMenuInvocation.CONTEXT_TARGET_SITE_MAP_TREE) {
					source = message.getRequest();
				}else {
					source = message.getResponse();
				}

				int[] selectedIndex = invocation.getSelectionBounds();
				//stdout.println(selectedIndex[0]+":"+selectedIndex[1]);

				if(source!=null && selectedIndex !=null && selectedIndex[1]-selectedIndex[0]>=3) {
					int selectedLength = selectedIndex[1]-selectedIndex[0];
					byte[] selectedBytes = new byte[selectedLength];
					System.arraycopy(source, selectedIndex[0], selectedBytes, 0, selectedLength);//新的内容替换选中内容
					String selectedUrl = new String(selectedBytes);
					//stdout.println(selectedUrl);
					if(!isFullUrl(selectedUrl)) {
						selectedUrl = message.getHttpService().toString()+"/"+selectedUrl;
					}else if(selectedUrl.startsWith("//")) {
						selectedUrl = message.getHttpService().getProtocol()+":"+selectedUrl;
					}
					open(selectedUrl,browserPath);
					//stdout.println(selectedUrl);
				}else {
					open(hosturl,browserPath);
				}
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}

	public static void open(Object url,String browser) throws Exception{
		String urlString = null;
		URI uri = null;
		if (url instanceof String) {
			urlString = (String) url;
			uri = new URI((String)url);
		}else if (url instanceof URL) {
			uri = ((URL)url).toURI();
			urlString = url.toString();
		}
		if(browser =="default" || browser=="") {
			Desktop desktop = Desktop.getDesktop();
			if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
				desktop.browse(uri);
			}
		}else {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(browser+" "+urlString);
			//C:\Program Files\Mozilla Firefox\firefox.exe
			//C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe
		}

	}

	public static boolean isFullUrl(String url) {
		if (url.startsWith("//") || url.startsWith("http://") || url.startsWith("https://")) {//    //misc.360buyimg.com/
			return true;
		}else if (url.startsWith("../") || url.startsWith("./") || url.startsWith("/")) {
			return false;
		}else {
			return true;
		}
	}
}
