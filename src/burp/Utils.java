package burp;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import U2C.CharSetHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;


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
			String[] cmdArray = new String[] {browser,urlString};

			//runtime.exec(browser+" "+urlString);//当命令中有空格时会有问题
			Runtime.getRuntime().exec(cmdArray);
		}
	}

	//拼接多个byte[]数组的方法
	public static byte[] join(byte[]... arrays)
	{
		int len = 0;
		for (byte[] arr : arrays)
		{
			len += arr.length;//计算多个数组的长度总和
		}

		byte[] result = new byte[len];
		int idx = 0;

		for (byte[] arr : arrays)
		{
			for (byte b : arr)
			{
				result[idx++] = b;
			}
		}

		return result;
	}
	/**
	 * 获取系统默认编码
	 * //https://javarevisited.blogspot.com/2012/01/get-set-default-character-encoding.html
	 * @return
	 */
	private static String getSystemCharSet() {
		return Charset.defaultCharset().toString();
	}
	
	/**
	 * 检测某个命令是否存在，根据which where命令来的，如果不在环境变量中应该读取不到！
	 */
	public static String isCommandExists(String cmd) {
        if (isWindows()) {
			cmd = "where "+cmd;
        }else {
			cmd = "which "+cmd;
        }
        try {
            //启动进程
			Process process = Runtime.getRuntime().exec(cmd);
            //获取输入流
            InputStream inputStream = process.getInputStream();
            //转成字符输入流
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, getSystemCharSet());
            int len = -1;
            char[] c = new char[1024];
            StringBuffer outputString = new StringBuffer();
            //读取进程输入流中的内容
            while ((len = inputStreamReader.read(c)) != -1) {
                String s = new String(c, 0, len);
                outputString.append(s);
                //System.out.print(s);
            }
            inputStream.close();
            return outputString.toString().trim();//去除换行符
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
	
	/**
	 * 换行符的可能性有三种，都必须考虑到
	 * @param input
	 * @return
	 */
	public static List<String> textToLines(String input){
		String[] lines = input.split("(\r\n|\r|\n)", -1);
		List<String> result = new ArrayList<String>();
		for(String line: lines) {
			line = line.trim();
			if (!line.equalsIgnoreCase("")) {
				result.add(line.trim());
			}
		}
		return result;
	}
	
	
	public static List<String> grepURL(String httpResponse) {
		httpResponse = httpResponse.toLowerCase();
		Set<String> URLs = new HashSet<>();
		Set<String> baseURLs = new HashSet<>();

		List<String> lines = textToLines(httpResponse);

		//https://github.com/GerbenJavado/LinkFinder/blob/master/linkfinder.py
		String regex_str = "(?:\"|')"
				+ "("
				+ "((?:[a-zA-Z]{1,10}://|//)[^\"'/]{1,}\\.[a-zA-Z]{2,}[^\"']{0,})"
				+ "|"
				+ "((?:/|\\.\\./|\\./)[^\"'><,;| *()(%%$^/\\\\\\[\\]][^\"'><,;|()]{1,})"
				+ "|"
				+ "([a-zA-Z0-9_\\-/]{1,}/[a-zA-Z0-9_\\-/]{1,}\\.(?:[a-zA-Z]{1,4}|action)(?:[\\?|/][^\"|']{0,}|))"
				+ "|"
				+ "([a-zA-Z0-9_\\-]{1,}\\.(?:php|asp|aspx|jsp|json|action|html|js|txt|xml)(?:\\?[^\"|']{0,}|))"
				+ ")"
				+ "(?:\"|')";

		//regex_str = Pattern.quote(regex_str);
		Pattern pt = Pattern.compile(regex_str);
		for (String line:lines) {//分行进行提取，似乎可以提高成功率？PATH_AND_QUERY
			line = decodeAll(line);
			Matcher matcher = pt.matcher(line);
			while (matcher.find()) {//多次查找
				String url = matcher.group();
				URLs.add(url);
			}
		}

		List<String> tmplist= new ArrayList<>(URLs);
		Collections.sort(tmplist);
		tmplist = removePrefixAndSuffix(tmplist,"\"","\"");
		tmplist = removePrefixAndSuffix(tmplist,"\'","\'");
		return tmplist;
	}
	
	public static List<String> removePrefixAndSuffix(List<String> input,String Prefix,String Suffix) {
		ArrayList<String> result = new ArrayList<String>();
		if (Prefix == null && Suffix == null) {
			return result;
		} else {
			if (Prefix == null) {
				Prefix = "";
			}

			if (Suffix == null) {
				Suffix = "";
			}

			List<String> content = input;
			for (String item:content) {
				if (item.startsWith(Prefix)) {
					//https://stackoverflow.com/questions/17225107/convert-java-string-to-string-compatible-with-a-regex-in-replaceall
					String tmp = Pattern.quote(Prefix);//自动实现正则转义
					item = item.replaceFirst(tmp, "");
				}
				if (item.endsWith(Suffix)) {
					String tmp = Pattern.quote(reverse(Suffix));//自动实现正则转义
					item = reverse(item).replaceFirst(tmp, "");
					item = reverse(item);
				}
				result.add(item); 
			}
			return result;
		}
	}

	public static String reverse(String str) {
		if (str == null) {
			return null;
		}
		return new StringBuffer(str).reverse().toString();
	}
	
	/**
	 * 先解Unicode，再解url，应该才是正确操作吧
	 * @param line
	 * @return
	 */
	public static String decodeAll(String line) {
		line = line.trim();

		if (needUnicodeConvert(line)) {
			while (true) {//unicode解码
				try {
					int oldlen = line.length();
					line = StringEscapeUtils.unescapeJava(line);
					int currentlen = line.length();
					if (oldlen > currentlen) {
						continue;
					}else {
						break;
					}
				}catch(Exception e) {
					//e.printStackTrace(BurpExtender.getStderr());
					break;//即使出错，也要进行后续的查找
				}
			}
		}

		if (needURLConvert(line)) {
			while (true) {
				try {
					int oldlen = line.length();
					line = URLDecoder.decode(line);
					int currentlen = line.length();
					if (oldlen > currentlen) {
						continue;
					}else {
						break;
					}
				}catch(Exception e) {
					//e.printStackTrace(BurpExtender.getStderr());
					break;//即使出错，也要进行后续的查找
				}
			}
		}

		return line;
	}
	
	public static boolean needUnicodeConvert(String str) {
		Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
		//Pattern pattern = Pattern.compile("(\\\\u([A-Fa-f0-9]{4}))");//和上面的效果一样
		Matcher matcher = pattern.matcher(str.toLowerCase());
		if (matcher.find() ){
			return true;
		}else {
			return false;
		}
	}

	public static boolean needURLConvert(String str) {
		Pattern pattern = Pattern.compile("(%(\\p{XDigit}{2}))");

		Matcher matcher = pattern.matcher(str.toLowerCase());
		if (matcher.find() ){
			return true;
		}else {
			return false;
		}
	}
	
	public static List<String> grepIPAndPort(String httpResponse) {
		Set<String> IPSet = new HashSet<>();
		String[] lines = httpResponse.split("(\r\n|\r|\n)");

		for (String line:lines) {
			String pattern = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?";
			Pattern pt = Pattern.compile(pattern);
			Matcher matcher = pt.matcher(line);
			while (matcher.find()) {//多次查找
				String tmpIP = matcher.group();
				IPSet.add(tmpIP);
			}
		}

		List<String> tmplist= new ArrayList<>(IPSet);
		Collections.sort(tmplist);
		return tmplist;
	}
	
	/**
	 * 对于信息收集来说，没有用的文件
	 * js是有用的
	 * pdf\doc\excel等也是有用的，可以收集到其中的域名
	 * rar\zip文件即使其中包含了有用信息，是无法直接读取的
	 * @param urlpath
	 * @return
	 */
	public static boolean uselessExtension(String urlpath) {
		String extensions = "css|jpeg|gif|jpg|png|rar|zip|svg|jpeg|ico|woff|woff2|ttf|otf|vue";
		String[] extList = extensions.split("\\|");
		for ( String item:extList) {
			if(urlpath.endsWith("."+item)) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println(isCommandExists("nmap1"));
	}

	/**
	 * 从Json文件中自动加载项目配置,可能会生成新文件
	 * @param callbacks
	 * @param configPath
	 */
	public static void autoLoadProjectConfig(IBurpExtenderCallbacks callbacks, String configPath) {
		if (configPath != null){
			//自动加载burp项目Json的配置 // Project.Config.json 支持相对(BurpSuitePro)和绝对路径
			String systemCharSet = CharSetHelper.getSystemCharSet();
			// 判断功能是否打开|功能打开后进行加载操作
			File file = new File(configPath);
			try{
				if (!file.exists() && !file.isDirectory()){
					//配置文件不存在时,自动根据当前的配置生成
					String configAsJson = callbacks.saveConfigAsJson();
					FileUtils.write(file,configAsJson,systemCharSet);
				}else {
					// 配置文件存在时,加载启动时加载项目配置文件
					callbacks.loadConfigFromJson(FileUtils.readFileToString(file, systemCharSet));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 保存当前的项目配置Json文件中,会覆盖旧文件
	 * @param callbacks
	 * @param configPath
	 */
	public static void autoSaveProjectConfig(IBurpExtenderCallbacks callbacks, String configPath) {
		if(configPath!=null){
			String systemCharSet = CharSetHelper.getSystemCharSet();
			File file = new File(configPath);
			try{
				//自动根据当前的配置存储配置文件
				String configAsJson = callbacks.saveConfigAsJson();
				FileUtils.write(file,configAsJson,systemCharSet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
