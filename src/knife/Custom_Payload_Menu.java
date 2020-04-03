package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.ConfigEntry;

/**
 *
 * @author bit4woo 
 */

//reference XXE_Menu.java
public class Custom_Payload_Menu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BurpExtender myburp;
	public String[] Custom_Payload_Menu;
	//sub level menu of hackbar++, the name of payload;


	public Custom_Payload_Menu(BurpExtender burp){
		this.setText("^_^ Custom Payload");
		this.myburp = burp;

		List<ConfigEntry> configs = burp.tableModel.getConfigByType(ConfigEntry.Config_Custom_Payload);
		Iterator<ConfigEntry> it = configs.iterator();
		List<String> tmp = new ArrayList<String>();
		while (it.hasNext()) {
			ConfigEntry item = it.next();
			tmp.add(item.getKey());//custom payload name
		}

		Custom_Payload_Menu = tmp.toArray(new String[0]);
		Methods.add_MenuItem_and_listener(this, Custom_Payload_Menu, new CustomPayloadItemListener(myburp));
	}
}

class CustomPayloadItemListener implements ActionListener {

	BurpExtender myburp;
	CustomPayloadItemListener(BurpExtender burp) {
		myburp = burp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {


		IHttpRequestResponse req = myburp.context.getSelectedMessages()[0];
		byte[] request = req.getRequest();

		int[] selectedIndex = myburp.context.getSelectionBounds();

		String action = e.getActionCommand();

		byte[] newRequest = GetNewRequest(request, selectedIndex, action);
		req.setRequest(newRequest);
	}

	public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){//action is the payload name

		//debug
		//PrintWriter stderr = new PrintWriter(myburp.callbacks.getStderr(), true);

		String payload =myburp.tableModel.getConfigByKey(action);

		String host = myburp.context.getSelectedMessages()[0].getHttpService().getHost();


		if (payload.contains("%host")) {
			payload = payload.replaceAll("%host", host);
		}
		//debug
		//stderr.println(payload);

		if(payload.toLowerCase().contains("%dnslogserver")) {
			String dnslog = myburp.tableModel.getConfigByKey("DNSlogServer");
			Pattern p = Pattern.compile("(?u)%dnslogserver");
			Matcher m  = p.matcher(payload);

			while ( m.find() ) {
				String found = m.group(0);
				payload = payload.replaceAll(found, dnslog);
			}
		}
		//debug
		//stderr.println(payload);

		if(payload!=null) {
			return Methods.do_modify_request(request, selectedIndex, payload.getBytes());
		}else {
			return request;
		}
	}
}
