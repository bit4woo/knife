package messageTab.U2C;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import com.bit4woo.utilbox.utils.ByteArrayUtils;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;

/**
 * @author bit4woo
 * @version CreateTime：2022年1月15日 下午11:07:59
 * <p>
 * 想要正确显示中文内容，有三个编码设置会影响结果：
 * 1、原始编码，通过代码尝试自动获取，但是结果可能不准确，极端情况下需要手动设置。
 * 2、转换后的编码，手动设置。
 * 3、burp设置的显示编码，显示时时用的编码，应该和转换后的编码一致。
 * <p>
 * 原始数据是byte[],但也是文本内容的某种编码的byte[].
 * @github https://github.com/bit4woo
 */
public class ChineseTab implements IMessageEditorTab {


	private ChinesePanel panel;

	private byte[] originContent;
	private String detectedCharset;
	private int charSetIndex = 0;


	public ChineseTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks) {
		panel = new ChinesePanel(this);
		BurpExtender.getCallbacks().customizeUiComponent(panel);//尝试使用burp的font size
	}


	@Override
	public String getTabCaption() {
		return "Chinese";
	}

	@Override
	public Component getUiComponent() {
		return panel;
	}

	/**
	 * 还是需要适当控制减少内存的占用
	 * 
	 *  Content-Type: image/x-icon
        Content-Type: image/png
        Content-Type: text/css
        Content-Type: font/woff2
        Content-Type: application/x-protobuf
	 */
	@Override
	public boolean isEnabled(byte[] content, boolean isRequest) {
		String contentType = BurpExtender.getHelperPlus().getHeaderValueOf(isRequest, content, "Content-Type");
		if (StringUtils.isEmpty(contentType)) {
			return true;
		}
		if (contentType.contains("image/")) {
			return false;
		}
		else if (contentType.contains("text/css")) {
			return false;
		}
		else if (contentType.contains("font/")) {
			return false;
		}
		else if (contentType.contains("x-protobuf")) {
			return false;
		}

		return true;
	}

	public byte[] getOriginContent() {
		return originContent;
	}

	public void setOriginContent(byte[] originContent) {
		this.originContent = originContent;
	}

	public List<String> getCharsetList() {
		String encoding = "UTF-8,GBK,GB2312,GB18030,Big5,Big5-HKSCS";
		List<String> encodingList = new ArrayList<>(Arrays.asList(encoding.split(",")));
		if (StringUtils.isNotEmpty(detectedCharset)) {
			encodingList.remove(detectedCharset);
			encodingList.add(0, detectedCharset);
		}
		return encodingList;
	}

	public String getCurrentCharSet() {
		return getCharsetList().get(charSetIndex);
	}

	public String getNextCharSet() {
		List<String> charsetList = getCharsetList();
		if (charSetIndex < charsetList.size() - 1) {
			charSetIndex = charSetIndex+1;
		} else {
			charSetIndex = 0;
		}
		return charsetList.get(charSetIndex);
	}

	@Override
	public void setMessage(byte[] content, boolean isRequest) {
		if (ByteArrayUtils.equals(originContent,content)) {
			return;
		}else {
			originContent = content;
			detectedCharset = BurpExtender.getHelperPlus().detectCharset(isRequest, content);
			panel.displayInChunks(content, isRequest, getCurrentCharSet(),1);
		}
	}

	/**
	 * 中文下的编辑还是有问题，暂不支持。
	 * 始终返回原始内容。
	 */
	@Override
	public byte[] getMessage() {
		return originContent;
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public byte[] getSelectedData() {
		//		return txtInput.getSelectedText();
		return null;
	}


	public static void main(String[] args) {
		String aaa = "STK_7411642209636022({\"errno\":1003,\"errmsg\":\"\\u7528\\u6237\\u672a\\u767b\\u5f55\",\"errmsg_lang\":{\"zh\":\"\\u7528\\u6237\\u672a\\u767b\\u5f55\",\"en\":\"User is not logged in.\",\"zh-HK\":\"\\u7528\\u6236\\u672a\\u767b\\u9304\"},\"data\":null});";
		System.out.println(StringEscapeUtils.unescapeJava(aaa));
		System.out.println(StringEscapeUtils.unescapeJava(aaa));
	}
}