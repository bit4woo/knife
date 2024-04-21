package messageTab.U2C;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import burp.IRequestInfo;
import burp.IResponseInfo;
import burp.ITextEditor;

/** 
 * @author bit4woo
 * @github https://github.com/bit4woo 
 * @version CreateTime：2022年1月15日 下午11:07:59 
 * 
 * 想要正确显示中文内容，有三个编码设置会影响结果：
 * 1、原始编码，通过代码尝试自动获取，但是结果可能不准确，极端情况下需要手动设置。
 * 2、转换后的编码，手动设置。
 * 3、burp设置的显示编码，显示时时用的编码，应该和转换后的编码一致。
 * 
 * 原始数据是byte[],但也是文本内容的某种编码的byte[].
 * 
 */
public class ChineseTab implements IMessageEditorTab{
	private ITextEditor txtInput;
	private JPanel panel;
	private JButton btnNewButton;

	private byte[] originContent;
	private byte[] handledOriginalContent = "Nothing to show".getBytes();

	private List<String> allPossibleCharset;
	private int charSetIndex = 0;

	private static IExtensionHelpers helpers;

	public ChineseTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		txtInput = callbacks.createTextEditor();
		panel = createpanel();
		txtInput.setEditable(editable);
		ChineseTab.helpers = helpers;
	}

	public String getCurrentCharSet() {
		return allPossibleCharset.get(charSetIndex);
	}

	public String getNextCharSet() {
		if (charSetIndex < allPossibleCharset.size()-1) {
			charSetIndex++;
		}else {
			charSetIndex =0;
		}
		return allPossibleCharset.get(charSetIndex);
	}

	public JPanel createpanel() {

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));

		btnNewButton = new JButton("Change Encoding");
		contentPane.add(btnNewButton, BorderLayout.NORTH);
		contentPane.add(txtInput.getComponent(), BorderLayout.CENTER);

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getNextCharSet();
				display();
				contentPane.repaint();
			}
		});

		return contentPane;
	}

	@Override
	public String getTabCaption()
	{
		return "Chinese";
	}

	@Override
	public Component getUiComponent()
	{
		return panel;
	}

	@Override
	public boolean isEnabled(byte[] content, boolean isRequest)
	{
		return true;
	}

	@Override
	public void setMessage(byte[] content, boolean isRequest)
	{
		String coding = "GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS";
		coding =coding +","+CharSetHelper.detectCharset(content);//检测到的编码+一些常用编码！
		allPossibleCharset = Arrays.asList(coding.split(","));

		if(content==null) {
			txtInput.setText(handledOriginalContent);
			return;
		}else {
			originContent = content;//存储原始数据
			handledOriginalContent = preHandle(content,isRequest,getCurrentCharSet());
			display();
		}
	}

	/**
	 * 使用特定编码显示内容,变化原始编码。
	 */
	public void display() {
		try {
			//每一次变化都应该取最开始的内容，否则一旦出错，后续的处理都是错的
			byte[] displayContent = CharSetHelper.covertCharSet(handledOriginalContent, getCurrentCharSet(), CharSetHelper.getSystemCharSet());
			txtInput.setText(displayContent);

			String text = String.format("Change Encoding: (Using %s)", getCurrentCharSet());
			btnNewButton.setText(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 中文下的编辑还是有问题，暂不支持。
	 * 始终返回原始内容。
	 */
	@Override
	public byte[] getMessage()
	{
		/*
		try {
			byte[] text = txtInput.getText();//这个时候应该是当前编码对应的byte[]
			byte[] result = new String(text,currentCharSet).getBytes(originalCharSet);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return originContent;
		}*/
		return originContent;
	}

	@Override
	public boolean isModified()
	{
		return false;
		//return txtInput.isTextModified();
	}

	@Override
	public byte[] getSelectedData()
	{
		return txtInput.getSelectedText();
	}

	public static boolean needtoconvert(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		Matcher matcher = pattern.matcher(str.toLowerCase());

		if (matcher.find() ){
			return true;
		}else {
			return false;
		}
	}

	public static boolean isJSON(byte[] content,boolean isRequest) {
		if (isRequest) {
			IRequestInfo requestInfo = helpers.analyzeRequest(content);
			return requestInfo.getContentType() == IRequestInfo.CONTENT_TYPE_JSON;
		} else {
			IResponseInfo responseInfo = helpers.analyzeResponse(content);
			return responseInfo.getInferredMimeType().equals("JSON");
		}
	}

	/**
	 * JSON美化，会自动转换Unicode
	 * @param inputJson
	 * @return
	 */
	public static String beauty(String inputJson) {
		//Take the input, determine request/response, parse as json, then print prettily.
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().serializeNulls().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(inputJson);//这段代码是在JSON中完成Unicode解码的关键，不能删除
		return gson.toJson(je);
	}

	/**
	 * 如果有Unicode编码的内容，就进行escape操作，否则内容和原始内容一致。
	 * @param content
	 * @param isRequest
	 * @return
	 */
	public static byte[] preHandle(byte[] content,boolean isRequest,String originalCharSet) {

		byte[] displayContent = content;

		try {
			String contentStr = new String(content,originalCharSet);
			if (needtoconvert(contentStr)){
				//先尝试进行JSON格式的美化，如果其中有Unicode编码也会自动完成转换
				if (isJSON(content, isRequest)) {
					try {
						Getter getter = new Getter(helpers);
						byte[] body = getter.getBody(isRequest, content);
						List<String> headers = getter.getHeaderList(isRequest, content);

						byte[] newBody = beauty(new String(body,originalCharSet)).getBytes(originalCharSet);

						displayContent = helpers.buildHttpMessage(headers,newBody);
						return displayContent;//如果JSON美化成功，主动返回。
					}catch(Exception e) {

					}
				}

				int i=0;
				do {
					contentStr = StringEscapeUtils.unescapeJava(contentStr);
					i++;
				}while(needtoconvert(contentStr) && i<3);

				displayContent = contentStr.getBytes(originalCharSet);
			}
		} catch (UnsupportedEncodingException e1) {

		}
		return displayContent;
	}

	public static void main(String[] args) {
		String aaa = "STK_7411642209636022({\"errno\":1003,\"errmsg\":\"\\u7528\\u6237\\u672a\\u767b\\u5f55\",\"errmsg_lang\":{\"zh\":\"\\u7528\\u6237\\u672a\\u767b\\u5f55\",\"en\":\"User is not logged in.\",\"zh-HK\":\"\\u7528\\u6236\\u672a\\u767b\\u9304\"},\"data\":null});";
		System.out.println(StringEscapeUtils.unescapeJava(aaa));
	}
}