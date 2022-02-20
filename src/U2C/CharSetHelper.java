package U2C;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import burp.BurpExtender;
import burp.Getter;
import burp.IExtensionHelpers;

public class CharSetHelper {

	public static String getSystemCharSet() {
		return Charset.defaultCharset().toString();
	}

	/**
	 * 进行响应包的编码转换。
	 * @param response
	 * @return 转换后的格式的byte[]
	 */
	public static byte[] covertCharSet(byte[] response,String originalCharset,String newCharset) {
		if (originalCharset == null) {
			return response;
		}else {
			byte[] newResponse;
			try {
				newResponse = new String(response,originalCharset).getBytes(newCharset);
				return newResponse;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(BurpExtender.getStderr());
				return response;
			}
		}
	}

	/**
	 * utf8 utf-8都是可以的。
	 * @param requestOrResponse
	 * @return
	 */
	public static String detectCharset(byte[] requestOrResponse){
		IExtensionHelpers helpers = BurpExtender.getCallbacks().getHelpers();
		Getter getter = new Getter(helpers);
		boolean isRequest = true;
		if (new String(requestOrResponse).startsWith("HTTP/")) {//response
			isRequest = false;
		}

		String contentType = getter.getHeaderValueOf(isRequest,requestOrResponse,"Content-Type");

		//1、尝试从contentTpye中获取
		if (contentType != null){
			if (contentType.toLowerCase().contains("charset=")) {
				String tmpcharSet = contentType.toLowerCase().split("charset=")[1];
				if (tmpcharSet != null && tmpcharSet.length() >0) {
					return tmpcharSet;
				}
			}
		}

		//2、尝试使用ICU4J进行编码的检测
		CharsetDetector detector = new CharsetDetector();
		detector.setText(requestOrResponse);
		CharsetMatch cm = detector.detect();
		if (cm != null) {
			return cm.getName();
		}

		//3、http post的默认编码
		return "ISO-8859-1";
	}
}

