package knife;

import javax.swing.JMenuItem;

import base.FindUrl_Action;
import burp.BurpExtender;


public class FindUrlAndRequest extends JMenuItem {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	//JMenuItem vs. JMenu
	public FindUrlAndRequest(BurpExtender burp) {
		this.setText("^_^ Find URL And Request");
		this.addActionListener(new FindUrl_Action(burp, burp.invocation));
	}

	public static void main(String[] args) {
		String url = "./abac/aaa.jpg";
		if (url.startsWith("./")) {
			url = url.replaceFirst("\\./", "");
		}
		System.out.println(url);
	}
}
