package U2C;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import burp.*;
import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

	private List<String> allPossibleCharset1; //环境编码列表
	private List<String> allPossibleCharset2; //转换编码列表
	private int charSetIndex1 = 0;
	private int charSetIndex2 = 0;
	private String preHandleCharSet = CharSetHelper.getSystemCharSet();
	private String detectCharset = CharSetHelper.getSystemCharSet();
	private String systemCharSet = CharSetHelper.getSystemCharSet(); ;

	private static IExtensionHelpers helpers;
	public BurpExtender burp;

	public ChineseTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		txtInput = callbacks.createTextEditor();
		panel = createpanel();
		txtInput.setEditable(editable);
		ChineseTab.helpers = helpers;
	}

	public String getCurrentCharSet1() {
		return allPossibleCharset1.get(charSetIndex1);
	}

	public String getCurrentCharSet2() {
		return allPossibleCharset2.get(charSetIndex2);
	}

	public String getNextCharSet1() {
		if (charSetIndex1 < allPossibleCharset1.size()-1) {
			charSetIndex1++;
		}else {
			charSetIndex1 =0;
		}
		return allPossibleCharset1.get(charSetIndex1);
	}

	public String getNextCharSet2() {
		if (charSetIndex2 < allPossibleCharset2.size()-1) {
			charSetIndex2++;
		}else {
			charSetIndex2 =0;
		}
		return allPossibleCharset2.get(charSetIndex2);
	}

	public JPanel createpanel() {

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));

		btnNewButton = new JButton("Current Encoding");
		contentPane.add(btnNewButton, BorderLayout.NORTH);
		contentPane.add(txtInput.getComponent(), BorderLayout.CENTER);

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				display();
				if ((charSetIndex2 + 1) % allPossibleCharset1.size() == 0){ getNextCharSet1();}
				getNextCharSet2();
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
		//根据用户输入的编码变量来获取系统显示编码
		String DisplayEncode = burp.tableModel.getConfigValueByKey("Display_Coding");
		if(null != DisplayEncode){	systemCharSet = DisplayEncode;}
		//System.out.println(String.format("User Input Display Encode -> %s", DisplayEncode ));

		String coding1 = "GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS,UNICODE,ISO-8859-1";
		String codingSetFrom = burp.tableModel.getConfigValueByKey("Coding_Set_From");
		if(null != codingSetFrom){ coding1 = codingSetFrom;}
		allPossibleCharset1 = Arrays.asList(coding1.split(","));

		String coding2 = "GBK,GB2312,UTF-8,GB18030,Big5,Big5-HKSCS,UNICODE,ISO-8859-1";
		String codingSetUsing = burp.tableModel.getConfigValueByKey("Coding_Set_Using");
		if(null != codingSetUsing){ coding2 = codingSetUsing;}
		detectCharset = CharSetHelper.detectCharset(content).toUpperCase();

		//如果detectCharset为ISO-8859-1,说明没有检测到编码,就用系统编码来解码数据,否则使用发现的编码来解码数据
		if("ISO-8859-1".equalsIgnoreCase(detectCharset)){
			preHandleCharSet = systemCharSet;
			coding2 = detectCharset + "," + coding2; //一些常用编码 + 检测到的编码！
		}else {
			preHandleCharSet = detectCharset;
			coding2 = coding2 + "," + detectCharset; //检测到的编码 + 一些常用编码！
		}
		List<String> tempList = Arrays.asList(coding2.split(","));
		LinkedHashSet<String> hashSet = new LinkedHashSet<>(tempList);
		allPossibleCharset2 = new ArrayList<>(hashSet); //去重

		if(content==null) {
			txtInput.setText(handledOriginalContent);
			return;
		}else {
			originContent = content;//存储原始数据
			preHandle(content, isRequest);
		}
	}


	/**
	 * 使用特定编码显示内容,变化原始编码。
	 * getCurrentCharSet1() from allPossibleCharset1
	 * getCurrentCharSet2() from allPossibleCharset2
	 */
	public void display() {
		try {
			String rawCharSet = getCurrentCharSet1();
			String newCharSet = getCurrentCharSet2();
			//每一次变化都应该取最开始的内容，否则一旦出错，后续的处理都是错的
			byte[] displayContent = CharSetHelper.covertCharSet(handledOriginalContent, rawCharSet, newCharSet);
			txtInput.setText(displayContent);
			String text = String.format("Current Encoding: (Base %s <-> Using %s)", rawCharSet, newCharSet);
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

	public static boolean needConvertUnicode(String str) {
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
		JsonElement je = jp.parse(inputJson);
		return gson.toJson(je);
	}

	/**
	 * 如果有Unicode编码的内容，就进行escape操作，否则内容和原始内容一致。
	 * @param content
	 * @param isRequest
	 * @return
	 */
	private void preHandle(byte[] content, boolean isRequest) {
		//String baseCharSet = CharSetHelper.getSystemCharSet();  //baseCharSet本编码应该与burp显示的编码强相关

		byte[] displayContent = content;
		String text = null;
		try {
			String contentStr = new String(content, preHandleCharSet);
			if (needConvertUnicode(contentStr)){
				//先尝试进行JSON格式的美化，如果其中有Unicode编码也会自动完成转换
				if (isJSON(content, isRequest)) {
					displayContent = formatJson(content, preHandleCharSet, isRequest);
				} else {
					int i=0;
					do {
						contentStr = StringEscapeUtils.unescapeJava(contentStr);
						i++;
					}while(needConvertUnicode(contentStr) && i<3);
					displayContent = contentStr.getBytes(preHandleCharSet);
				}

				//如果检测出来的编码与系统显示编码不一致，可以使用系统编码进行一次转换，一般都是一致的
				if(preHandleCharSet != systemCharSet){
					displayContent = CharSetHelper.covertCharSet(displayContent, preHandleCharSet, systemCharSet);
				}
				//按钮显示内容
				text = String.format("Current Encoding: (Base %s <-> Using %s)", preHandleCharSet, systemCharSet);
			}else {
				//如果内容检测的编码和系统编码不一样,需要使用检测到的编码来进行一次转换
				if(detectCharset != systemCharSet){
					displayContent = CharSetHelper.covertCharSet(content, detectCharset, systemCharSet);

					//此处增加Json格式化处理
					if(isJsonContent(displayContent, systemCharSet, isRequest) ){
						displayContent = formatJson(displayContent, systemCharSet, isRequest);
					}
					//按钮显示内容
					text = String.format("Current Encoding: (Base %s <-> Using %s)", detectCharset, systemCharSet);
				}
			}
		} catch (UnsupportedEncodingException e1) {}
		handledOriginalContent = displayContent;
		txtInput.setText(handledOriginalContent);//设置响应框初步显示的内容,后续可以对内容进行格式化
		btnNewButton.setText(text);//设置按钮显示内容
	}

	/**
	 * 简单判断报文内容是不是Json格式
	 * @param Content
	 * @param ContentCharSet
	 * @param isRequest
	 * @return
	 */
	private boolean isJsonContent(byte[] Content, String ContentCharSet, boolean isRequest) {
		try {
			Getter getter = new Getter(helpers);
			byte[] body = getter.getBody(isRequest, Content);
			List<String> headers = getter.getHeaderList(isRequest, Content);
			//通过判断头部内容
			for (String header : headers) {
				if(header.contains("application/json")){
					return true;
				}
			}
			//直接判断内容格式
			String beautyJSON = beauty(new String(body, ContentCharSet));
			if(isLikeJson(beautyJSON)){
				return true;
			}
		}catch(Exception e) {
			System.out.println(String.format("format json error %s", e.getMessage()));
			return false;
		}
		return false;
	}

	/**
	 * 判断字符串是否是Json格式
	 * @param obj
	 * @return
	 */
	public static boolean isLikeJson(Object obj) {
		try{
			String str = obj.toString().trim();
			if (str.charAt(0) == '{' && str.charAt(str.length() - 1) == '}') {
				return true;
			}
			return false;
		}catch (Exception e){
			return false;
		}
	}

	/**
	 * 格式化Json内容
	 * @param Content
	 * @param isRequest
	 * @return
	 */
	private byte[] formatJson(byte[] Content, String ContentCharSet, boolean isRequest) {
		try {
			Getter getter = new Getter(helpers);
			byte[] body = getter.getBody(isRequest, Content);
			List<String> headers = getter.getHeaderList(isRequest, Content);
			String beautyJSON = beauty(new String(body, ContentCharSet));
			byte[] newBody = beautyJSON.getBytes(ContentCharSet);
			Content = helpers.buildHttpMessage(headers,newBody);
		}catch(Exception e) {
			System.out.println(String.format("format json error %s", e.getMessage()));
		}
		return Content;
	}


	public static void main(String[] args) {
		String aaa = "STK_7411642209636022({\"errno\":1003,\"errmsg\":\"\\u7528\\u6237\\u672a\\u767b\\u5f55\",\"errmsg_lang\":{\"zh\":\"\\u7528\\u6237\\u672a\\u767b\\u5f55\",\"en\":\"User is not logged in.\",\"zh-HK\":\"\\u7528\\u6236\\u672a\\u767b\\u9304\"},\"data\":null});";
		System.out.println(StringEscapeUtils.unescapeJava(aaa));
	}
}