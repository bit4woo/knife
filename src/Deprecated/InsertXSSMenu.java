package Deprecated;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JMenuItem;

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


@Deprecated
public class InsertXSSMenu extends JMenuItem {
	//JMenuItem vs. JMenu
	public InsertXSSMenu(BurpExtender burp){
		if (burp.invocation.getInvocationContext() == IContextMenuInvocation.CONTEXT_MESSAGE_EDITOR_REQUEST) {
			if (burp.tableModel.getConfigValueByKey("XSS-Payload")!=null){
				this.setText("^_^ Insert XSS");
				this.addActionListener(new InsertXSSAction(burp,burp.invocation));
			}
		}
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
		byte[] newRequest = messageInfo.getRequest();//为了不影响原始request，通过final进行一次转换

		Getter getter = new Getter(helpers);
		List<IParameter> paras = getter.getParas(messageInfo);
		String xsspayload = burp.tableModel.getConfigValueByKey("XSS-Payload");
		String charset = CharSetHelper.detectCharset(newRequest);

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

	public static void test() {
		//System.out.println(isInt("13175192849"));
		String aaa = "{\r\n" + 
				" \"person\":{\"name\":\"Sam\", \"surname\":\"ngonma\"},\r\n" + 
				" \"car\":{\"make\":\"toyota\", \"model\":\"yaris\"}\r\n" + 
				" }";
		String bbb = "[\r\n" + 
				"  {\r\n" + 
				"    \"amount\": \" 12185\",\r\n" + 
				"    \"job\": \"GAPA\",\r\n" + 
				"    \"month\": \"JANUARY\",\r\n" + 
				"    \"year\": \"2010\"\r\n" + 
				"  },\r\n" + 
				"  {\r\n" + 
				"    \"amount\": \"147421\",\r\n" + 
				"    \"job\": \"GAPA\",\r\n" + 
				"    \"month\": \"MAY\",\r\n" + 
				"    \"year\": \"2010\"\r\n" + 
				"  },\r\n" + 
				"  {\r\n" + 
				"    \"amount\": \"2347\",\r\n" + 
				"    \"job\": \"GAPA\",\r\n" + 
				"    \"month\": \"AUGUST\",\r\n" + 
				"    \"year\": \"2010\"\r\n" + 
				"  }\r\n" + 
				"]";
		try {
			System.out.println(updateJSONValue(bbb,"11111"));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws Exception {
		String aaa = "[1,2]";
		String bbb = "['a','b']";
		String ccc = "{\r\n" + 
				" \"person\":{\"name\":\"Sam\", \"surname\":\"ngonma\"},\r\n" + 
				" \"car\":{\"make\":\"toyota\", \"model\":\"yaris\"}\r\n" + 
				" }";
		String ddd = "['a':'b',xxxx,1111]";
		System.out.println(isJSONArray(bbb));
		System.out.println(updateJSONValue(bbb,"xxx"));
	}
}