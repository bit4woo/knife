package burp;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import burp.Getter;

public class CharSet {
	private static String getSystemCharSet() {
		return Charset.defaultCharset().toString();
	}
	
	public static byte[] covertCharSetToByte(byte[] response) {
		String originalCharSet = getResponseCharset(response);
		//BurpExtender.getStderr().println(url+"---"+originalCharSet);
		if (originalCharSet == null) {
			return response;
		}else {
			byte[] newResponse;
			try {
				newResponse = new String(response,originalCharSet).getBytes(getSystemCharSet());
				return newResponse;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(BurpExtender.getStderr());
				return response;
			}
		}
	}
	
	public static String getResponseCharset(byte[] response){
		Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
		String contentType = getter.getHeaderValueOf(false,response,"Content-Type");
		String body = new String(getter.getBody(false,response));
		String tmpcharSet = null;

		if (contentType != null){//1、尝试从contentTpye中获取
			if (contentType.toLowerCase().contains("charset=")) {
				tmpcharSet = contentType.toLowerCase().split("charset=")[1];
			}
		}

		if (tmpcharSet == null){//2、尝试从body中获取
			Pattern pDomainNameOnly = Pattern.compile("charset=(.*?)>");
			Matcher matcher = pDomainNameOnly.matcher(body);
			if (matcher.find()) {
				tmpcharSet = matcher.group(0).toLowerCase();
				//				tmpcharSet = tmpcharSet.replace("\"","");
				//				tmpcharSet = tmpcharSet.replace(">","");
				//				tmpcharSet = tmpcharSet.replace("/","");
				//				tmpcharSet = tmpcharSet.replace("charset=","");
			}
		}

		if (tmpcharSet == null){//3、尝试使用ICU4J进行编码的检测
			CharsetDetector detector = new CharsetDetector();
			detector.setText(response);
			CharsetMatch cm = detector.detect();
			tmpcharSet = cm.getName();
		}

		tmpcharSet = tmpcharSet.toLowerCase().trim();
		if (tmpcharSet.contains("utf8")){
			tmpcharSet = "utf-8";
		}else {
			//常见的编码格式有ASCII、ANSI、GBK、GB2312、UTF-8、GB18030和UNICODE等。
			List<String> commonCharSet = Arrays.asList("ASCII,ANSI,GBK,GB2312,UTF-8,GB18030,UNICODE,ISO-8859-1".toLowerCase().split(","));
			for (String item:commonCharSet) {
				if (tmpcharSet.contains(item)) {
					tmpcharSet = item;
				}
			}
		}
		return tmpcharSet;
	}
}

