package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;

public class SetCookieMenu extends JMenuItem {
	//JMenuItem vs. JMenu

	private static final long serialVersionUID = 1L;

	public SetCookieMenu(BurpExtender burp){
		this.setText("^_^ Set Cookie");
		this.addActionListener(new SetCookie_Action(burp,burp.context));
	}
}

class SetCookie_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public SetCookie_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		HeaderEntry cookieEntry = CookieUtils.getLatestCookieFromSpeicified();
		if (cookieEntry != null) {//当没有找到相应的cookie时为null
			try{
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();
				for(IHttpRequestResponse message:messages) {
					Getter getter = new Getter(helpers);
					String targetShortUrl = getter.getShortUrl(message);
					cookieEntry.setTargetUrl(targetShortUrl);
					this.burp.config.getSetCookieMap().put(targetShortUrl, cookieEntry);
					//让proxy处理程序，处理响应包的更新
				}
				this.burp.config.setUsedCookie(cookieEntry);
			}
			catch (Exception e1)
			{
				e1.printStackTrace(stderr);
			}
		}
	}
}
