package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.SystemUtils;
import com.bit4woo.utilbox.utils.UrlUtils;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;


public class CopyJsOfThisSite extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public CopyJsOfThisSite(BurpExtender burp){
		this.setText("^_^ Copy JS Of This Site");
		this.addActionListener(new CopyJsOfThisSite_Action(burp,burp.invocation));
	}

}

class CopyJsOfThisSite_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public CopyJsOfThisSite_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
		this.stdout = BurpExtender.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Runnable Runner = new Runnable() {
			@Override
			public void run() {
				try{
					IHttpRequestResponse[] messages = invocation.getSelectedMessages();
					if (messages == null || messages.length <=0) {
						return;
					}
					try {
						String content = findUrls(messages[0]);
						SystemUtils.writeToClipboard(content);
					} catch (Exception e) {
						e.printStackTrace(BurpExtender.getStderr());
					}
				}
				catch (Exception e1)
				{
					e1.printStackTrace(stderr);
				}
			}


			/**
			 * 根据当前web的baseUrl找JS，特征就是referer以它开头
			 * @param message
			 * @return 
			 * @return
			 */
			public String findUrls(IHttpRequestResponse message){
				HelperPlus getter = new HelperPlus(helpers);

				List<String> JsCode = new ArrayList<>();
				String current_referUrl = getter.getHeaderValueOf(true,message,"Referer");
				String current_fullUrl = getter.getFullURL(message).toString();


				String siteBaseUrl = null;
				if (current_fullUrl != null) {
					siteBaseUrl = UrlUtils.getBaseUrl(current_referUrl);
				}
				if (siteBaseUrl == null) {
					siteBaseUrl = UrlUtils.getBaseUrl(current_fullUrl);
				}

				if (StringUtils.isEmpty(siteBaseUrl)){
					return "";
				}

				IHttpRequestResponse[] messages = BurpExtender.getCallbacks().getSiteMap(null);
				for (IHttpRequestResponse item:messages) {
					int code = getter.getStatusCode(item);
					URL url = getter.getFullURL(item);
					String referUrl = getter.getHeaderValueOf(true,item,"Referer");
					if (referUrl == null ||  url== null || code <=0) {
						continue;
					}
					if (!url.toString().toLowerCase().endsWith(".js") || !url.toString().toLowerCase().endsWith(".js.map")) {
						continue;
					}

					if (referUrl.toLowerCase().startsWith(siteBaseUrl.toLowerCase())) {
						byte[] respBody = HelperPlus.getBody(false, item);
						String body = new String(respBody);
						JsCode.add(url.toString());
						JsCode.add(body);
					}
				}

				return String.join(System.lineSeparator(), JsCode);
			}

		};
		new Thread(Runner).start();
	}
}
