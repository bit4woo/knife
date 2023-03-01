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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import U2C.CharSetHelper;
import com.google.gson.*;
import config.GUI;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import sun.net.util.IPAddressUtil;


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
	 * 从Json文件中自动加载项目配置,可能会生成新文件,追加表单配置
	 * @param callbacks
	 */
	public static void autoLoadProjectConfig(IBurpExtenderCallbacks callbacks, boolean addDefaultExcludeHosts) {
		String configPath  = GUI.tableModel.getConfigValueByKey("Auto_Load_Project_Config_On_Startup");
		if (configPath!=null){
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

			//是否添加用户输入的配置文件
			if(addDefaultExcludeHosts){
				addDefaultExcludeHosts(callbacks);
			}
		}
	}

	/**
	 * 保存当前的项目配置Json文件中,会覆盖旧文件
	 * @param callbacks
	 */
	public static void autoSaveProjectConfig(IBurpExtenderCallbacks callbacks) {
		String configPath  = GUI.tableModel.getConfigValueByKey("Auto_Load_Project_Config_On_Startup");
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

	/**
	 * 根据变量判断是否保存当前的项目配置修改到Json文件中
	 * @param callbacks
	 */
	public static void autoSaveProjectConfigWithFlag(IBurpExtenderCallbacks callbacks){
		String autoSaveFlag  = GUI.tableModel.getConfigValueByKey("Auto_Save_Config_After_Update_Scope");
		if(autoSaveFlag!=null){
			autoSaveProjectConfig(callbacks);
		}
	}

	/**
	 * 去重JsonArray,输入的Array里面时Json对象
	 * @param jsonObjectJsonArray
	 * @param jsonObjectKey
	 * @return
	 */
	public static JsonArray DeDuplicateJsonObjectJsonArray(JsonArray jsonObjectJsonArray , String jsonObjectKey){
		HashSet<String> hashSet = new HashSet<>();
		JsonArray resultJsonArray = new JsonArray();
		List<JsonObject> list = new ArrayList<>();
		for (int i = 0; i < jsonObjectJsonArray.size(); i++){
			JsonObject jsonObject = jsonObjectJsonArray.get(i).getAsJsonObject();
			String jsonElement = jsonObject.get(jsonObjectKey).getAsString();
			if (!hashSet.contains(jsonElement)){
				list.add(jsonObject);
				hashSet.add(jsonElement);
			}
		}
		for ( JsonObject jsonObject : list){
			resultJsonArray.add(jsonObject);
		}
		return resultJsonArray;
	}

	/**
	 * 去除JsonArray里面指定键 并且 值的元素
	 * @param jsonObjectJsonArray
	 * @param jsonObjectKey
	 * @param jsonObjectValue
	 * @return
	 */
	public static JsonArray RemoveJsonObjectJsonArray(JsonArray jsonObjectJsonArray , String jsonObjectKey, String jsonObjectValue){
		JsonArray resultJsonArray = new JsonArray();
		List<JsonObject> list = new ArrayList<>();
		for (int i = 0; i < jsonObjectJsonArray.size(); i++){
			JsonObject jsonObject = jsonObjectJsonArray.get(i).getAsJsonObject();
			String jsonElement = jsonObject.get(jsonObjectKey).getAsString();
			if (!jsonObjectValue.equals(jsonElement)){
				list.add(jsonObject);
			}
		}
		for (JsonObject jsonObject : list){
			resultJsonArray.add(jsonObject);
		}
		return resultJsonArray;
	}

	/**
	 * 去除JsonArray里面指定键 并且 值包含在hastset中的元素
	 * @param jsonObjectJsonArray
	 * @param jsonObjectKey
	 * @param hashSet
	 * @return
	 */
	public static JsonArray RemoveJsonObjectJsonArray(JsonArray jsonObjectJsonArray , String jsonObjectKey, HashSet<String> hashSet){
		JsonArray resultJsonArray = new JsonArray();
		List<JsonObject> list = new ArrayList<>();
		for (int i = 0; i < jsonObjectJsonArray.size(); i++){
			JsonObject jsonObject = jsonObjectJsonArray.get(i).getAsJsonObject();
			String jsonElement = jsonObject.get(jsonObjectKey).getAsString();
			if (!hashSet.contains(jsonElement)){
				list.add(jsonObject);
			}
		}
		for (JsonObject jsonObject : list){
			resultJsonArray.add(jsonObject);
		}
		return resultJsonArray;
	}

	/**
	 * 添加主机名到排除列表
	 * @param callbacks
	 * @param hostHashSet
	 */
	public static void AddHostToExScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks, HashSet<String> hostHashSet) {
		//不处理没有获取到host的情况
		if(hostHashSet.size()>0){
			// 1、读取当前的配置文件
			String configContent = callbacks.saveConfigAsJson();
			JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();
			//开起前置条件

			//高级模式开关 //设置高级模式
			jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().addProperty("advanced_mode",true);

			//生成ExcludeJson元素 并循环添加到json对象中
			JsonArray excludeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("exclude").getAsJsonArray();
			for(String host:hostHashSet){
				HashMap<String,Object> aExcludeHashMap = new HashMap();
				aExcludeHashMap.put("enabled",true);
				aExcludeHashMap.put("host",host);
				aExcludeHashMap.put("protocol","any");
				String excludeJsonString =new Gson().toJson(aExcludeHashMap);
				JsonObject excludeJsonObject = JsonParser.parseString(excludeJsonString).getAsJsonObject();
				excludeJsonArray.add(excludeJsonObject);
			}

			//去重Json对象的排除列表
			JsonArray removeDuplicateJsonArray = DeDuplicateJsonObjectJsonArray(excludeJsonArray,"host");
			jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("exclude",removeDuplicateJsonArray);

			//判断包含列表是否存在和排除列表相同的数据
			JsonArray includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
			if(includeJsonArray.size()>0){
				//去除包含列表中和排除列表相同的数据
				JsonArray removeJsonObjectJsonArray = RemoveJsonObjectJsonArray(includeJsonArray,"host",hostHashSet);
				jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("include",removeJsonObjectJsonArray);
			}

			//判断包含列表是否为空  //如果include Scope为空需要修改为.* //不然全部删除
			//includeJsonArray 内存地址改变，需要重新获取,
			includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
			if(includeJsonArray.size()<1){
				//设置include Scope为.*
				HashMap<String,Object> aIncludeHashMap = new HashMap();
				aIncludeHashMap.put("enabled",true);
				aIncludeHashMap.put("host",".*");
				aIncludeHashMap.put("protocol","any");
				String includeJsonString = new Gson().toJson(aIncludeHashMap);
				JsonObject includeJsonObject = JsonParser.parseString(includeJsonString).getAsJsonObject();
				includeJsonArray.add(includeJsonObject);
			}
			//加载Json文件
			String jsonObjectString = new Gson().toJson(jsonObject);
			callbacks.loadConfigFromJson(jsonObjectString);

			//根据用户设置,保存当前内存的配置到Json配置到文件
			autoSaveProjectConfigWithFlag(callbacks);
		}
	}

	/**
	 * 添加主机名到包含列表
	 * @param callbacks
	 * @param hostHashSet
	 */
	public static void AddHostToInScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks, HashSet<String> hostHashSet) {
		//不处理没有获取到host的情况
		if(hostHashSet.size()>0){
			// 1、读取当前的配置文件
			String configContent = callbacks.saveConfigAsJson();
			JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();
			//开起前置条件

			//高级模式开关 //设置高级模式
			jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().addProperty("advanced_mode",true);

			//生成IncludeJson元素 并循环添加到json对象中
			JsonArray includeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("include").getAsJsonArray();
			for(String host:hostHashSet){
				HashMap<String,Object> aIncludeHashMap = new HashMap();
				aIncludeHashMap.put("enabled",true);
				aIncludeHashMap.put("host",host);
				aIncludeHashMap.put("protocol","any");
				String includeJsonString =new Gson().toJson(aIncludeHashMap);
				JsonObject includeJsonObject = JsonParser.parseString(includeJsonString).getAsJsonObject();
				includeJsonArray.add(includeJsonObject);
			}
			//去重Json对象的包含列表
			JsonArray removeDuplicateJsonArray = DeDuplicateJsonObjectJsonArray(includeJsonArray,"host");
			//删除包含列表里面.*的对象不然没有意义
			removeDuplicateJsonArray = RemoveJsonObjectJsonArray(removeDuplicateJsonArray,"host",".*");
			//将修改后的数据保存到json里面
			jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("include",removeDuplicateJsonArray);

			//去除排除列表中和包含列表相同的数据
			JsonArray excludeJsonArray = jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().get("exclude").getAsJsonArray();
			if(excludeJsonArray.size()>0){
				JsonArray removeJsonObjectJsonArray = RemoveJsonObjectJsonArray(excludeJsonArray,"host",hostHashSet);
				jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("exclude",removeJsonObjectJsonArray);
			}

			//加载生成的Json配置到应用
			String jsonObjectString = new Gson().toJson(jsonObject);
			callbacks.loadConfigFromJson(jsonObjectString);

			//根据用户设置,保存当前内存的配置到Json配置到文件
			autoSaveProjectConfigWithFlag(callbacks);
		}
	}

	/**
	 * 清空所有Scope内容
	 * @param callbacks
	 */
	public static void ClearAllScopeAdvByProjectConfig(IBurpExtenderCallbacks callbacks) {
		// 1、读取当前的配置文件
		String configContent = callbacks.saveConfigAsJson();
		JsonObject jsonObject = JsonParser.parseString(configContent).getAsJsonObject();

		//生成IncludeJson元素 清空元素
		jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("include",new JsonArray());
		jsonObject.get("target").getAsJsonObject().get("scope").getAsJsonObject().add("exclude",new JsonArray());

		//加载生成的Json配置到应用
		String jsonObjectString = new Gson().toJson(jsonObject);
		callbacks.loadConfigFromJson(jsonObjectString);

		//根据用户设置,保存当前内存的配置到Json配置到文件
		autoSaveProjectConfigWithFlag(callbacks);
	}

	/**
	 * 追加Auto_Append_Hosts表单设置到配置文件的排除列表中
	 * @param callbacks
	 */
	public static void addDefaultExcludeHosts(IBurpExtenderCallbacks callbacks) {
		String defaultExcludeHosts  = GUI.tableModel.getConfigValueByKey("Auto_Append_Hosts_To_Exclude_Scope");
		if (defaultExcludeHosts!=null && defaultExcludeHosts.trim().length()>0){
			HashSet<String> hashSet = new HashSet<>();
			//切割并整理输入
			List<String> defaultExcludeHostList = Arrays.asList(defaultExcludeHosts.split(","));
			for(String host:defaultExcludeHostList){
				hashSet.add(host.trim());
			}
			//添加主机名到排除列表
			AddHostToExScopeAdvByProjectConfig(callbacks, hashSet);
		}
	}


	/**
	 * 将[.]替换为[\.],便于进行正则精确匹配
	 * @param host
	 * @return
	 */
	public static String dotToEscapeDot(String host ) {
		return host.replace(".","\\.");
	}

	/**
	 * 域名转为上级域名格式 www.baidu.com -> baidu.com
	 * @param domain
	 * @return
	 */
	public static String domainToSuperiorDomain(String domain){
		// 获取上级域名 3级域名获取2级域名|2级域名获取主域名|主域名不操作
		String[] hostParts = domain.split("\\.");
		if (hostParts.length > 2) {
			String[] slicedArr = Arrays.copyOfRange(hostParts, 1, hostParts.length);
			domain = String.join(".", slicedArr);
		}
		return domain;
	}

	/**
	 * 判断Host不是IPv4或者IPv6格式
	 * @param host
	 * @return
	 */
	public static boolean isIPFormat(String host) {
		boolean isIpv4 = IPAddressUtil.isIPv4LiteralAddress(host);
		boolean isIpv6 = IPAddressUtil.isIPv6LiteralAddress(host);
		return isIpv4||isIpv6;
	}

	/**
	 * 将域名变为转移的上级域名转义正则 www.xxx.com -> .*\.xxx\.com IP仅转义.号
	 * @param host
	 * @return
	 */
	public static String hostToWildcardHostWithDotEscape(String host) {
		if(isIPFormat(host)){
			return dotToEscapeDot(domainToSuperiorDomain(host));
		}else {
			return ".*" + "\\." + dotToEscapeDot(domainToSuperiorDomain(host));
		}
	}
}
