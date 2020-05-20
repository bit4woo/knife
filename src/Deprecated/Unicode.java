package Deprecated;

import org.apache.commons.text.StringEscapeUtils;



/**
 * 弃用该类，改为使用StringEscapeUtils.unescapeJava
 */
@Deprecated
public class Unicode {

	/**
	 * unicode解码（unicode编码转中文）
	 * 
	 * @param theString
	 * @return
	 */
	public static String unicodeDecode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);

				if (aChar == 'u') {
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException(
									"Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
	public static void main(String[] args) throws Exception {
		//String unicodestring = "\u624b\u673a\u9a8c\u8bc1\u5931\u8d25\uff0c\u8bf7\u91cd\u65b0\u5c1d\u8bd5";
		String unicodestring = "{\"code\":404,\"message\":\"\u624b\u673a\u9a8c\u8bc1\u5931\u8d25\uff0c\u8bf7\u91cd\u65b0\u5c1d\u8bd5\"}";
		String badexample= "rtrim = /^[\\s\\uFEFF\\xA0]+|[\\s\\uFEFF\\xA0]+$/g,";
		String xx = "\"[[\\\"\\\\u591a\\\\u5143\\\\u6587\\\\u5316\\\\uff0c\\\\u4e3b\\\\u9898\\\\u516c\\\\u56ed\\\\u4e4b\\\\u90fd\\\"]";
		String chineseCharacter = "aaaa\\uff01\\u0040\\u0023\\uffe5\\u0025\\u2026\\u2026\\u0026\\u002a\\uff08\\uff09\\u2014\\u2014\\u002d\\u003d\\uff0c\\u3002\\uff1b\\uff1a\\u201c\\u2018\\u007b\\u007d\\u3010\\u3011\\u002b";
		String chineseCharacter1 = "aaaa\\ufffe";
		System.out.println(unicodeDecode(unicodeDecode(chineseCharacter)));
		System.out.println(StringEscapeUtils.unescapeJava(chineseCharacter));
		}
	//url for test: https://csdnimg.cn/static/api/js/view/share_view.js?v=3ae6026d.js
}