package knife;

import burp.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

public class InsertXSSMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public InsertXSSMenu(BurpExtender burp){
		this.setText("^_^ Insert XSS");
		this.addActionListener(new InsertXSSAction(burp,burp.context));
	}
}

class InsertXSSAction implements ActionListener {
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public InsertXSSAction(BurpExtender burp, IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		IHttpRequestResponse[] selectedItems = invocation.getSelectedMessages();

		IHttpRequestResponse messageInfo = selectedItems[0];
		byte[] newRequest = messageInfo.getRequest();

		Getter getter = new Getter(helpers);
		List<IParameter> paras = getter.getParas(messageInfo);
		String xsspayload = burp.tableModel.getConfigByKey("XSS-Payload");

		if (xsspayload == null) return;

		for(IParameter para:paras) {
			if (para.getType() == IParameter.PARAM_COOKIE) {
				continue;
			}

			String value = para.getValue();

			if (isInt(value)) {
				continue;
			}

			if (para.getType() == IParameter.PARAM_JSON) {
				System.out.println(para.getValue());
			}

			if (para.getType() == IParameter.PARAM_URL) {
				value = value + helpers.urlEncode(xsspayload);
			}else{
				value = value+xsspayload;
			}



			IParameter newPara = helpers.buildParameter(para.getName(), value, para.getType());
			newRequest = helpers.updateParameter(newRequest, newPara);
		}
		messageInfo.setRequest(newRequest);
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

	public static JSONObject updateJSONValue(JSONObject obj, String newValue) throws Exception {
		// We need to know keys of Jsonobject
		Iterator iterator = obj.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();

			// if object is just string we change value in key
			if ((obj.optJSONArray(key)==null) && (obj.optJSONObject(key)==null)) {
				// put new value
				String value = (String) obj.get(key);
				obj.put(key, value+newValue);
			}

			// if it's jsonobject
			if (obj.optJSONObject(key) != null) {
				updateJSONValue(obj.getJSONObject(key), newValue);
			}

			// if it's jsonarray
			if (obj.optJSONArray(key) != null) {
				JSONArray jArray = obj.getJSONArray(key);
				JSONArray jArrayResult =new JSONArray();
				for (int i=0;i<=jArray.length();i++) {
					JSONObject newJSONObject = updateJSONValue(jArray.getJSONObject(i), newValue);
					jArrayResult.put(newJSONObject);
				}
				obj.put(key, jArrayResult);
			}
		}
		return obj;
	}

	public static void main(String[] args) {
		//System.out.println(isInt("13175192849"));
		String aaa = "{\r\n" + 
				" \"person\":{\"name\":\"Sam\", \"surname\":\"ngonma\"},\r\n" + 
				" \"car\":{\"make\":\"toyota\", \"model\":\"yaris\"}\r\n" + 
				" }";
		new JSONObject(aaa);
		try {
			System.out.println(updateJSONValue(new JSONObject(aaa),"11111"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}