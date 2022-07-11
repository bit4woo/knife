package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import com.github.kevinsawicki.http.HttpRequest;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.Utils;


public class FindUrlAndRequest extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public FindUrlAndRequest(BurpExtender burp){
		this.setText("^_^ Find URL And Request");
		this.addActionListener(new FindUrl_Action(burp,burp.invocation));
	}
}

class FindUrl_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public FindUrl_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
		this.stdout = BurpExtender.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Runnable requestRunner = new Runnable() {
			@Override
			public void run() {
				try{
					IHttpRequestResponse[] messages = invocation.getSelectedMessages();
					HelperPlus getter = new HelperPlus(helpers);
					if (messages == null) {
						return;
					}

					for (IHttpRequestResponse message:messages) {
						try {
							byte[] respBody = getter.getBody(false, message);
							if (null == respBody) {
								continue;
							}
							String body = new String(respBody);
							List<String> urls = Utils.grepURL(body);
							Set<String> baseUrls = getBaseURL(urls);
							
							String referUrl = getter.getHeaderValueOf(true,message,"Referer");
							baseUrls.add(referUrl);
							String fullUrl = getter.getFullURL(message).toString();
							baseUrls.add(fullUrl);
							
							String baseurl = choseAndEditBaseURL(baseUrls);
							
							if (null==baseurl) {
								return;
							}
							
							for (String url:urls) {
								if (!url.startsWith("http://") && !url.startsWith("https://")) {
									url = baseurl+url;
									sendRequest(url);
								}
							}
						} catch (Exception e) {
							e.printStackTrace(BurpExtender.getStderr());
						}
					}
				}
				catch (Exception e1)
				{
					e1.printStackTrace(stderr);
				}
			}
		};
		new Thread(requestRunner).start();
	}
	
	public static Set<String> getBaseURL(List<String> urls) {
		Set<String> baseURLs = new HashSet<String>();
		for (String tmpurl:urls) {
			//这部分提取的是含有协议头的完整URL地址
			if (tmpurl.toLowerCase().startsWith("http://")
					||tmpurl.toLowerCase().startsWith("https://")){
				baseURLs.add(tmpurl);
			}
		}
		return baseURLs;
	}

	public static String choseAndEditBaseURL(Set<String> domains) {

		int n = domains.size()+1;
		String[] possibleValues = new String[n];

		// Copying contents of domains to arr[]
		System.arraycopy(domains.toArray(), 0, possibleValues, 0, n-1);
		possibleValues[n-1] = "";

		String selectedValue = (String) JOptionPane.showInputDialog(null,
				"Choose One", "Chose And Edit Base URL",
				JOptionPane.INFORMATION_MESSAGE, null,
				possibleValues, possibleValues[0]);
		if (null != selectedValue) {
			String baseUrl = JOptionPane.showInputDialog("Confirm The Base URL", selectedValue).trim();
			return baseUrl;
		}
		return selectedValue;
	}

	public static void sendRequest(String url) {
		HttpRequest request = HttpRequest.get(url);
		//Configure proxy
		request.useProxy("127.0.0.1", 8080);

		//Accept all certificates
		request.trustAllCerts();
		//Accept all hostnames
		request.trustAllHosts();

		request.code();


		HttpRequest postRequest = HttpRequest.post(url);
		//Configure proxy
		postRequest.useProxy("127.0.0.1", 8080);
		//Accept all certificates
		postRequest.trustAllCerts();
		//Accept all hostnames
		postRequest.trustAllHosts();

		postRequest.send("test=test");
		postRequest.code();

	}


	public static void main(String[] args) {

	}
}
