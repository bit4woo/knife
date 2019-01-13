package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JMenu;

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


	Custom_Payload_Menu(BurpExtender burp){
		this.setText("Custom Payload");
		this.myburp = burp;

		HashMap<String,String> config = burp.config.getBasicConfigs();
		Iterator<String> it = config.keySet().iterator();
		List<String> tmp = new ArrayList<String>();
		while (it.hasNext()) {
			String item = it.next();
			if (item.startsWith("<payload>")) {
				item = item.replace("<payload>", "");
				tmp.add(item);
			}
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

	public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){

		//debug
		//PrintWriter stderr = new PrintWriter(myburp.callbacks.getStderr(), true);

		HashMap<String,String> payloadMap = new HashMap<String,String>();
		HashMap<String,String> config = myburp.config.getBasicConfigs();
		Iterator<Entry<String,String>> it = config.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String,String> entry = it.next();
			String key = entry.getKey();
			String value = entry.getValue();
			if (key.startsWith("<payload>")) {
				key = key.replace("<payload>", "");
			}
			payloadMap.put(key, value);
		}

		String payload =payloadMap.get(action);

		String host = myburp.context.getSelectedMessages()[0].getHttpService().getHost();


		if (payload.contains("%host")) {
			payload = payload.replaceAll("%host", host);
		}
		//debug
		//stderr.println(payload);

		if(payload.toLowerCase().contains("%dnslogserver")) {
			String dnslog = config.get("DNSlogServer");
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
