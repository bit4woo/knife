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
		this.addActionListener(new SetCookie_Action(burp,burp.invocation));
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
		try{
			//stdout.println("SetCookie_Action called");
			HeaderEntry cookieEntry = CookieUtils.getLatestCookieFromSpeicified();

			if (cookieEntry != null) {//当没有找到相应的cookie时为null
				IHttpRequestResponse[] messages = invocation.getSelectedMessages();
				if (invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
					byte[] newRequest = CookieUtils.updateCookie(messages[0], cookieEntry.getHeaderValue());
					try{
						messages[0].setRequest(newRequest);
					}catch (Exception e){
						e.printStackTrace(stderr);
						//stderr.print(e.getMessage());
						//这是个bug，请求包实际还是被修改了，但是就是报这个错误！
						//当在proxy中拦截状态下更新请求包的时候，总是会报这个假错误！
						//"java.lang.UnsupportedOperationException: Request has already been issued"
					}
					cookieEntry.setRequestUpdated(true);//表明请求包已经被更新
					stdout.print("1111updated....");
				}

				for(IHttpRequestResponse message:messages) {
					Getter getter = new Getter(helpers);
					String targetShortUrl = getter.getShortURL(message).toString();
					cookieEntry.setTargetUrl(targetShortUrl);
					this.burp.config.getSetCookieMap().put(targetShortUrl, cookieEntry);
					//让proxy处理函数processProxyMessage()去处理响应包的更新
				}
				this.burp.config.setUsedCookie(cookieEntry);
			}else {
				stderr.println("No cookie found with your input");
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace(stderr);
		}
	}
}
