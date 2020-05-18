package U2C;

import java.awt.Component;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import burp.IMessageEditorTabFactory;
import burp.IRequestInfo;
import burp.IResponseInfo;
import burp.ITextEditor;

public class JSONBeautifier implements IMessageEditorTab,IMessageEditorTabFactory
{
	private ITextEditor txtInput;
	private byte[] originContent;
	private IExtensionHelpers helpers;
	public JSONBeautifier(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		txtInput = callbacks.createTextEditor();
		txtInput.setEditable(editable);
		this.helpers = helpers;
	}

	@Override
	public String getTabCaption()
	{
		return "JSON";
	}

	@Override
	public Component getUiComponent()
	{
		return txtInput.getComponent();
	}

	@Override
	public boolean isEnabled(byte[] content, boolean isRequest)
	{   
		try {
			if (content== null) {
				return false;
			}
			originContent = content;
			if (isRequest) {
				IRequestInfo requestInfo = helpers.analyzeRequest(content);
				return requestInfo.getContentType() == IRequestInfo.CONTENT_TYPE_JSON;
			} else {
				IResponseInfo responseInfo = helpers.analyzeResponse(content);
				return responseInfo.getInferredMimeType().equals("JSON");
			}
		} catch (Exception e) {
			e.printStackTrace();
			e.printStackTrace(BurpExtender.getStderr());
			return false;
		}
	}

	@Override
	public void setMessage(byte[] content, boolean isRequest)
	{    	
		try {
			String jsonBody = "";
			if (content == null) {
				// clear our display
				txtInput.setText("none".getBytes());
				txtInput.setEditable(false);
			} else {
				//Get only the JSON part of the content
				Getter getter = new Getter(helpers);
				byte[] body = getter.getBody(isRequest, content);
				List<String> headers = getter.getHeaderList(isRequest, content);

				byte[] newContet = helpers.buildHttpMessage(headers, beauty(new String(body)).getBytes());
				//newContet = CharSet.covertCharSetToByte(newContet);

				txtInput.setText(newContet);
			}
		} catch (Exception e) {
			txtInput.setText(e.getStackTrace().toString().getBytes());
		}
	}
	
	public static String beauty(String inputJson) {
		//Take the input, determine request/response, parse as json, then print prettily.
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(inputJson);
		return gson.toJson(je);
	}

	@Override
	public byte[] getMessage()
	{
		//byte[] text = txtInput.getText();
		//return text;
		return originContent;
		//change the return value of getMessage() method to the origin content to tell burp don't change the original response

	}

	@Override
	public boolean isModified()
	{
		//return txtInput.isTextModified();
		return false;
		//change the return value of isModified() method to false. to let burp don't change the original response) 
	}

	@Override
	public byte[] getSelectedData()
	{
		return txtInput.getSelectedText();
	}

	public static void main(String args[]) {
		String aaa = "{\"returnCode\":\"0\",\"description\":\"success\",\"resJson\":\"{\\\"userRouteInfoList\\\":[{\\\"bankVerifyState\\\":1,\\\"channelRoleTypes\\\":[{\\\"allowLogin\\\":1,\\\"innerStatus\\\":0,\\\"roleClassify\\\":1,\\\"roleID\\\":\\\"SMRole\\\"}],\\\"checkExpire\\\":2,\\\"countryCode\\\":\\\"CN\\\",\\\"lastLoginTime\\\":\\\"2020-05-11 08:37:12\\\",\\\"realName\\\":\\\"小明\\\",\\\"siteID\\\":1,\\\"userID\\\":\\\"1\\\",\\\"userType\\\":1,\\\"userValidStatus\\\":1,\\\"verifyRealState\\\":2}],\\\"resultCode\\\":0}\"}";
		System.out.print(beauty(aaa));
	}

	@Override
	public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
		return new JSONBeautifier(null, false, BurpExtender.callbacks.getHelpers(), BurpExtender.callbacks);
	}
}