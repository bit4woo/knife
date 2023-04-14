package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import U2C.CharSetHelper;
import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;
import burp.Methods;
import config.ConfigEntry;

/**
 * 将某个payload插入所有的插入点，比如XSS
 * @author bit4woo 
 */

//reference XXE_Menu.java
public class CustomPayloadForAllInsertpointMenu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public BurpExtender burp;
	public String[] Custom_Payload_Menu;

	public CustomPayloadForAllInsertpointMenu(BurpExtender burp){
		try {
			this.setText("^_^ Insert Payload For All");
			this.burp = burp;

			List<ConfigEntry> configs = burp.tableModel.getConfigByType(ConfigEntry.Config_Custom_Payload);
			List<ConfigEntry> configs1 = burp.tableModel.getConfigByType(ConfigEntry.Config_Custom_Payload_Base64);
			configs.addAll(configs1);
			Iterator<ConfigEntry> it = configs.iterator();
			List<String> tmp = new ArrayList<String>();
			while (it.hasNext()) {
				ConfigEntry item = it.next();
				tmp.add(item.getKey());//custom payload name
			}

			Custom_Payload_Menu = tmp.toArray(new String[0]);
			Methods.add_MenuItem_and_listener(this, Custom_Payload_Menu, new ForAllInserpointListener(burp));
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
		}
	}
}

class ForAllInserpointListener implements ActionListener {
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public ForAllInserpointListener(BurpExtender burp) {
		this.burp = burp;
		this.invocation = burp.invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();
		IHttpRequestResponse messageInfo = selectedItems[0];
		byte[] newRequest = messageInfo.getRequest();//为了不影响原始request，通过final进行一次转换

		Getter getter = new Getter(helpers);
		List<IParameter> paras = getter.getParas(messageInfo);
		
		String charset = CharSetHelper.detectCharset(newRequest);
		String xsspayload;
		try {
			xsspayload = new String(getPayload(event.getActionCommand()),charset);
		} catch (UnsupportedEncodingException e1) {
			xsspayload = new String(getPayload(event.getActionCommand()));
		}

		if (xsspayload == null) return;

		boolean jsonHandled = false;
		for(IParameter para:paras) {
			String value = para.getValue();
			byte type = para.getType();
			if (type == IParameter.PARAM_COOKIE || isInt(value)) {
				continue;
			}else if (type == IParameter.PARAM_JSON ) {//json参数的更新方法，这里只是针对body是json
				if (!jsonHandled){
					//stdout.println(para.getValue());
					List<String> headers = helpers.analyzeRequest(newRequest).getHeaders();
					try {
						String body = new String(getter.getBody(true,newRequest),charset);
						if (isJSON(body)){
							body = updateJSONValue(body,xsspayload).toString();
							newRequest = helpers.buildHttpMessage(headers,body.getBytes(charset));
							jsonHandled = true;
						}
					} catch (Exception e) {
						e.printStackTrace(stderr);
					}
				}
			}else {
				if (type == IParameter.PARAM_URL) {//url中的参数需要编码
					value = helpers.urlDecode(value);
				}
				if (isJSON(value)){//当参数的值是json格式
					try {
						value = updateJSONValue(value,xsspayload).toString();
					} catch (Exception e) {
						e.printStackTrace(stderr);
					}
				}else {
					value = value+xsspayload;
				}

				if (type == IParameter.PARAM_URL) {//url中的参数需要编码
					value = helpers.urlEncode(value);
				}
				IParameter newPara = helpers.buildParameter(para.getName(), value, para.getType());
				newRequest = helpers.updateParameter(newRequest, newPara);
			}
		}
		messageInfo.setRequest(newRequest);
	}
	

	public byte[] getPayload(String action){//action is the payload name

		//debug
		//PrintWriter stderr = new PrintWriter(myburp.callbacks.getStderr(), true);
		
		byte[] payloadBytes = null;
		String payload =burp.tableModel.getConfigValueByKey(action);

		if (burp.tableModel.getConfigTypeByKey(action).equals(ConfigEntry.Config_Custom_Payload)) {
			
			String host = burp.invocation.getSelectedMessages()[0].getHttpService().getHost();


			if (payload.contains("%host")) {
				payload = payload.replaceAll("%host", host);
			}
			//debug
			//stderr.println(payload);

			if(payload.toLowerCase().contains("%dnslogserver")) {
				String dnslog = burp.tableModel.getConfigValueByKey("DNSlogServer");
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
		

		if (burp.tableModel.getConfigTypeByKey(action).equals(ConfigEntry.Config_Custom_Payload_Base64)) {
			payloadBytes = Base64.getDecoder().decode(payload);
			//用IExtensionHelpers的stringToBytes bytesToString方法来转换的话？能保证准确性吗？
		}
		
		return payloadBytes;
	}

	public static boolean isInt(String input) {
		try {
			Integer b = Integer.valueOf(input);
			return true;
		} catch (NumberFormatException e) {
			try {
				long l = Long.valueOf(input);
				return true;
			}catch(Exception e1) {

			}
			return false;
		}
	}

	public static boolean isJSON(String test) {
		if (isJSONObject(test) || isJSONArray(test)) {
			return true;
		}else {
			return false;
		}
	}

	//org.json
	public static boolean isJSONObject(String test) {
		try {
			new JSONObject(test);
			return true;
		} catch (JSONException ex) {
			return false;
		}
	}


	public static boolean isJSONArray(String test) {
		try {
			new JSONArray(test);
			return true;
		} catch (JSONException ex) {
			return false;
		}
	}

	//org.json
	public static String updateJSONValue(String JSONString, String payload) throws Exception {

		if (isJSONObject(JSONString)) {
			JSONObject obj = new JSONObject(JSONString);
			Iterator<String> iterator = obj.keys();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();		// We need to know keys of Jsonobject
				String value = obj.get(key).toString();


				if (isJSONObject(value)) {// if it's jsonobject
					String newValue = updateJSONValue(value, payload);
					obj.put(key,new JSONObject(newValue));
				}else if (isJSONArray(value)) {// if it's jsonarray
					String newValue = updateJSONValue(value, payload);
					obj.put(key,new JSONArray(newValue));
				}else {
					if (!isBooleanOrNumber(value)){
						obj.put(key, value+payload);
					}
				}
			}
			return obj.toString();
		}else if(isJSONArray(JSONString)) {
			JSONArray jArray = new JSONArray(JSONString);

			ArrayList<String> newjArray = new ArrayList<String>();
			for (int i=0;i<jArray.length();i++) {//无论Array中的元素是JSONObject还是String都转换成String进行处理即可
				String item = jArray.get(i).toString();
				String newitem = updateJSONValue(item,payload);
				newjArray.add(newitem);
			}
			return newjArray.toString();
		}else {
			return JSONString+payload;
		}
	}

	public static boolean isBooleanOrNumber(String input) {
		if (input.toLowerCase().equals("true") || input.toLowerCase().equals("false")){
			return true;
		}else{
			return isNumeric(input);
		}
	}

	public static boolean isNumeric(String str){
		for(int i=str.length();--i>=0;){
			int chr=str.charAt(i);
			if(chr<48 || chr>57) {
				return false;
			}
		}
		return true;
	}
}
