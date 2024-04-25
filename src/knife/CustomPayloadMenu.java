package knife;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.ConfigEntry;
import config.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author bit4woo 
 */
public class CustomPayloadMenu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BurpExtender myburp;
	public CustomPayloadMenu(BurpExtender burp){
		try {
			this.setText("^_^ Insert Payload");
			this.myburp = burp;

			List<ConfigEntry> configs = GUI.tableModel.getConfigByType(ConfigEntry.Config_Custom_Payload);
			List<ConfigEntry> configs1 = GUI.tableModel.getConfigByType(ConfigEntry.Config_Custom_Payload_Base64);
			configs.addAll(configs1);
			for (ConfigEntry config:configs){
				String name = config.getKey();
				JMenuItem item = new JMenuItem(name);
				item.addActionListener(new CustomPayloadItemListener(burp));
				add(item);
			}
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
	}
}

class CustomPayloadItemListener implements ActionListener {

	BurpExtender myburp;
	CustomPayloadItemListener(BurpExtender burp) {
		myburp = burp;
	}

	@Override
	public void actionPerformed(ActionEvent e) {


		IHttpRequestResponse req = myburp.invocation.getSelectedMessages()[0];
		byte[] request = req.getRequest();

		int[] selectedIndex = myburp.invocation.getSelectionBounds();

		String action = e.getActionCommand();

		byte[] newRequest = GetNewRequest(request, selectedIndex, action);
		req.setRequest(newRequest);
	}

	public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){//action is the payload name

		//debug
		//PrintWriter stderr = new PrintWriter(myburp.callbacks.getStderr(), true);

		byte[] payloadBytes = null;
		String payload =myburp.tableModel.getConfigValueByKey(action);

		if (myburp.tableModel.getConfigTypeByKey(action).equals(ConfigEntry.Config_Custom_Payload)) {

			String host = myburp.invocation.getSelectedMessages()[0].getHttpService().getHost();


			if (payload.contains("%host")) {
				payload = payload.replaceAll("%host", host);
			}
			//debug
			//stderr.println(payload);

			if(payload.toLowerCase().contains("%dnslogserver")) {
				String dnslog = myburp.tableModel.getConfigValueByKey("DNSlogServer");
				if (dnslog == null) {
					dnslog = "dnslog.com";
				}
				Pattern p = Pattern.compile("(?i)%dnslogserver");
				Matcher m  = p.matcher(payload);

				while ( m.find() ) {
					String found = m.group(0);
					payload = payload.replaceAll(found, dnslog);
				}
			}
			//debug
			//stderr.println(payload);
			payloadBytes = payload.getBytes();
		}


		if (myburp.tableModel.getConfigTypeByKey(action).equals(ConfigEntry.Config_Custom_Payload_Base64)) {
			payloadBytes = Base64.getDecoder().decode(payload);
			//用IExtensionHelpers的stringToBytes bytesToString方法来转换的话？能保证准确性吗？
		}


		if(payloadBytes!=null) {
			return Methods.do_modify_request(request, selectedIndex, payloadBytes);
		}else {
			return request;
		}
	}
}
