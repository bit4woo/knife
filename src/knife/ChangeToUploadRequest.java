package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.JMenuItem;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IHttpRequestResponse;

/**
 *
 * @author bit4woo 
 */

public class ChangeToUploadRequest extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ChangeToUploadRequest(BurpExtender burp){
		try {
			this.setText("^_^ Change To Upload Request");
			this.addActionListener(new ChangeToUploadRequestListener(burp));
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
	}
}

class ChangeToUploadRequestListener implements ActionListener {

	BurpExtender myburp;
	ChangeToUploadRequestListener(BurpExtender burp) {
		myburp = burp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		IHttpRequestResponse message = myburp.invocation.getSelectedMessages()[0];
		HelperPlus getter = new HelperPlus(BurpExtender.getCallbacks().getHelpers());

		byte[] newRequestBytes = message.getRequest();
		if (getter.getMethod(message).equalsIgnoreCase("GET")) {
			newRequestBytes = BurpExtender.getCallbacks().getHelpers().toggleRequestMethod(newRequestBytes);
		}

		String boundary = "---------------------------"+generateRandomString(30);
		newRequestBytes = getter.addOrUpdateHeader(true, newRequestBytes, "Content-Type", "multipart/form-data; boundary="+boundary);

		String body = boundary+"\r\n"
				+ "Content-Disposition: form-data; name=\"uploadImage\"; filename=\"phpinfo.png\"\r\n"
				+ "Content-Type: image/png\r\n"
				+ "\r\n"
				+ "<?php phpinfo();?>\r\n"
				+ boundary;
		newRequestBytes = getter.UpdateBody(true, newRequestBytes, body.getBytes());

		message.setRequest(newRequestBytes);
	}


	public static String generateRandomString(int length) {
		//String sourceString = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String sourceString = "0123456789";
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(sourceString.length());
			char randomChar = sourceString.charAt(index);
			sb.append(randomChar);
		}
		return sb.toString();
	}

}
