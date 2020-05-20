package burp;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

public class Utils {
	public static boolean isWindows() {
		String OS_NAME = System.getProperties().getProperty("os.name").toLowerCase();
		//System.out.println(OS_NAME);
		if (OS_NAME.contains("windows")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isWindows10() {
		String OS_NAME = System.getProperties().getProperty("os.name").toLowerCase();
		if (OS_NAME.equalsIgnoreCase("windows 10")) {
			return true;
		}
		return false;
	}

	public static boolean isMac(){
		String os = System.getProperty("os.name").toLowerCase();
		//Mac
		return (os.indexOf( "mac" ) >= 0); 
	}

	public static boolean isUnix(){
		String os = System.getProperty("os.name").toLowerCase();
		//linux or unix
		return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
	}


	public static void browserOpen(Object url,String browser) throws Exception{
		String urlString = null;
		URI uri = null;
		if (url instanceof String) {
			urlString = (String) url;
			uri = new URI((String)url);
		}else if (url instanceof URL) {
			uri = ((URL)url).toURI();
			urlString = url.toString();
		}
		if(browser == null ||browser.equalsIgnoreCase("default") || browser.equalsIgnoreCase("")) {
			//whether null must be the first
			Desktop desktop = Desktop.getDesktop();
			if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
				desktop.browse(uri);
			}
		}else {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(browser+" "+urlString);
			//C:\Program Files\Mozilla Firefox\firefox.exe
			//C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe
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
