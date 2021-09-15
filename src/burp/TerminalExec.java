package burp;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;

/*
 * 在系统terminal中执行命令，实现思路：
 * 1、将命令写入bat文件
 * 2、通过执行bat文件执行命令
 */
public class TerminalExec {

	/*
	 * 通知执行bat文件来执行命令
	 */
	public static Process runBatchFile(String batfilepath) {
		String command = "";
		if (Utils.isWindows()) {
			command="cmd /c start " + batfilepath;
		} else {
			if (new File("/bin/sh").exists()) {
				command="/bin/sh " + batfilepath;
			}
			else if (new File("/bin/bash").exists()) {
				command="/bin/bash " + batfilepath;
			}
		}
		try {
			Process process = Runtime.getRuntime().exec(command);
			process.waitFor();//等待执行完成
			return process;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String genBatchFile(String cmdContent, String batchFileName) {
		try {
			//将命令写入剪切板
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(cmdContent);
			clipboard.setContents(selection, null);

			if (batchFileName == null || batchFileName.trim().equals("")) {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd-HHmmss");
				String timeString = simpleDateFormat.format(new Date());
				batchFileName = timeString+".bat";
			}else if(!batchFileName.endsWith(".bat") && !batchFileName.endsWith(".cmd")) {
				batchFileName = batchFileName+".bat";
			}

			String workdir = System.getProperty("user.home");
			File batFile = new File(workdir,batchFileName);
			if (!batFile.exists()) {
				batFile.createNewFile();
			}
			if (Utils.isMac()){
				cmdContent = String.format("osascript -e 'tell app \"Terminal\" to do script \"%s\"'",cmdContent);
			}
			FileUtils.writeByteArrayToFile(batFile, cmdContent.getBytes());
			return batFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * 切换工作目录
	 */
	public String changeDirCommand(String dir){
		//运行命令的工作目录，work path
		String command = "cd "+dir+System.lineSeparator();

		if (Utils.isWindows()) {//如果是windows，还要注意不同磁盘的切换
			String diskString = dir.split(":")[0];
			command =command+ diskString+":"+System.lineSeparator();
		}
		return command;
	}

	/*
	 * parserPath --- python.exe java.exe ....
	 * executerPath --- sqlmap.py nmap.exe ....
	 * parameters ---- -v -A -r xxx.file .....
	 */
	public static String genCmd(String parserPath,String executerPath, String parameter) {
		StringBuilder command = new StringBuilder();

		if (parserPath != null){
			if (parserPath.contains(" ")) {
				parserPath = "\""+parserPath+"\"";//如果路径中包含空格，需要引号
			}
			command.append(parserPath);
			command.append(" ");
		}

		if (executerPath != null){

			if (executerPath.contains(" ")) {
				executerPath = "\""+executerPath+"\"";//如果路径中包含空格，需要引号
			}

			command.append(executerPath);
			command.append(" ");
		}

		if (parameter != null && !parameter.equals("")) {
			command.append(parameter);
		}
		command.append(System.lineSeparator());
		return command.toString();
	}

	/*
	 * 判断某个文件是否在环境变量中
	 */
	public static boolean isInEnvironmentPath(String filename) {
		if (filename == null) {
			return false;
		}
		Map<String, String> values = System.getenv();
		String pathvalue = values.get("PATH");
		if (pathvalue == null) {
			pathvalue = values.get("path");
		}
		if (pathvalue == null) {
			pathvalue = values.get("Path");
		}
		//		System.out.println(pathvalue);
		String[] items = pathvalue.split(";");
		for (String item:items) {
			File tmpPath = new File(item);
			if (tmpPath.isDirectory()) {
				//				System.out.println(Arrays.asList(tmpPath.listFiles()));
				File fullpath = new File(item,filename);
				if (Arrays.asList(tmpPath.listFiles()).contains(fullpath)) {
					return true;
				}else {
					continue;
				}
			}
		}
		return false;
	}

	public static void main(String[] args) {
		System.out.println(isInEnvironmentPath("nmap.exe"));
	}
}