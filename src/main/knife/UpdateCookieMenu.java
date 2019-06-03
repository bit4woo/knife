package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import javax.swing.JMenuItem;
import burp.*;

public class UpdateCookieMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public UpdateCookieMenu(BurpExtender burp){
		this.setText("^_^ Update Cookie");
		this.addActionListener(new UpdateCookieAction(burp,burp.context));
	}
}

class UpdateCookieAction implements ActionListener {
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public UpdateCookieAction(BurpExtender burp, IContextMenuInvocation invocation) {
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

		String shorturl = selectedItems[0].getHttpService().toString();
		String latestCookie = CookieUtils.getLatestCookieFromHistory(shorturl);//自行查找一次
		
		int time = 0;
		while (!isVaildCookie(latestCookie) && time <2) {
			latestCookie = CookieUtils.getLatestCookieFromSpeicified();
			time++;
		}
		
		if (isVaildCookie(latestCookie)) {
			String latestCookieValue = latestCookie.split(CookieUtils.SPLITER)[1];
			shorturl = latestCookie.split(CookieUtils.SPLITER)[0];
			
			byte[] newRequest = CookieUtils.updateCookie(selectedItems[0], latestCookieValue);
			selectedItems[0].setRequest(newRequest);

			if (shorturl.startsWith("http")) {
				this.burp.config.getTmpMap().put("UsedCookie", latestCookie);
			}
		}else {
			//do nothing
		}
	}
	
	public boolean isVaildCookie(String urlAndCookieString) {
		if (urlAndCookieString == null || urlAndCookieString == "") {
			return false;
		}
		if (!urlAndCookieString.contains(CookieUtils.SPLITER)) {
			return false;
		}
		String currentCookie = new Getter(helpers).getHeaderValueOf(true,invocation.getSelectedMessages()[0],"Cookie");
		String foundCookie = urlAndCookieString.split(CookieUtils.SPLITER)[1];
		if (foundCookie.equalsIgnoreCase(currentCookie)) {
			return false;
		}
		return true;
	}
}