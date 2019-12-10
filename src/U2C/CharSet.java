package U2C;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import burp.BurpExtender;
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
	
	private static String getResponseCharset(byte[] response){
		Getter getter = new Getter(BurpExtender.callbacks.getHelpers());
		String contentType = getter.getHeaderValueOf(false,response,"Content-Type");
		String tmpcharSet = null;
		if (contentType != null && contentType.toLowerCase().contains("charset=")){
			tmpcharSet = contentType.toLowerCase().split("charset=")[1];
		}else {
			String body = new String(getter.getBody(false,response));
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
		//常见的编码格式有ASCII、ANSI、GBK、GB2312、UTF-8、GB18030和UNICODE等。
		List<String> commonCharSet = Arrays.asList("ASCII,ANSI,GBK,GB2312,UTF-8,GB18030,UNICODE,ISO-8859-1".toLowerCase().split(","));
		if (tmpcharSet == null){
			return null;
		}else if (tmpcharSet.contains("utf8")){
			return "utf-8";
		}else {
			for (String item:commonCharSet) {
				if (tmpcharSet.contains(item)) {
					return item;
				}
			}
		}
		return null;
	}
}

