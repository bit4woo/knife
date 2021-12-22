package burp;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;

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
	
	public static void main(String[] args) {
		System.out.println(isCommandExists("nmap1"));
	}
	
}
