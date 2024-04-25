package messageTab.Info;

import com.bit4woo.utilbox.burp.HelperPlus;

import burp.BurpExtender;

public class InfoGrepper {

	public InfoGrepper(byte[] content, boolean isRequest) {
		HelperPlus getter = new HelperPlus(BurpExtender.getCallbacks().getHelpers());
		
		byte[] body = HelperPlus.getBody(isRequest, content);
		
		if (body!= null) {
			String bodyStr = new String(body);
			
		}
		
	}
	
	
	public InfoGrepper(String content, boolean isRequest) {
		
	}

}
