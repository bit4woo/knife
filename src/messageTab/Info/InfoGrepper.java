package messageTab.Info;

import burp.BurpExtender;
import burp.HelperPlus;

public class InfoGrepper {

	public InfoGrepper(byte[] content, boolean isRequest) {
		HelperPlus getter = new HelperPlus(BurpExtender.getCallbacks().getHelpers());
		
		byte[] body = getter.getBody(isRequest, content);
		
		if (body!= null) {
			String bodyStr = new String(body);
			
		}
		
	}
	
	
	public InfoGrepper(String content, boolean isRequest) {
		
	}

}
