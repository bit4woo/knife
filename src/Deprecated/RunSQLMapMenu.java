package Deprecated;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JMenuItem;

import org.apache.commons.io.FileUtils;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.Utils;

@Deprecated
public class RunSQLMapMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public RunSQLMapMenu(BurpExtender burp){
		this.setText("^_^ Run SQLMap");
		this.addActionListener(new RunSQLMap_Action(burp,burp.context));
	}
}

class RunSQLMap_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public RunSQLMap_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Runnable SqlmapRunner = new Runnable() {
			@Override
			public void run() {
				try{
					IHttpRequestResponse[] messages = invocation.getSelectedMessages();
					if (messages !=null) {
						IHttpRequestResponse message = messages[0];
						String requestFilePath = RequestToFile(message);
						String batFilePathString  = genbatFile(requestFilePath);
						String command = SQLMapCommand(batFilePathString);
						Process process = Runtime.getRuntime().exec(command);
					}
				}
				catch (Exception e1)
				{
					e1.printStackTrace(stderr);
				}
			}
		};
		new Thread(SqlmapRunner).start();
	}

	/*
	 * 请求包存入文件
	 */
	public String RequestToFile(IHttpRequestResponse message) {
		try {
			Getter getter = new Getter(helpers);
			String host = getter.getHost(message);
			SimpleDateFormat simpleDateFormat = 
					new SimpleDateFormat("MMdd-HHmmss");
			String timeString = simpleDateFormat.format(new Date());
			String filename = host+"."+timeString+".req";

			String basedir = (String) System.getProperties().get("java.io.tmpdir");
			String configBasedir = burp.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
			if (configBasedir != null && new File(configBasedir).exists()) {
				basedir = configBasedir;
			}

			File requestFile = new File(basedir,filename);
			FileUtils.writeByteArrayToFile(requestFile, message.getRequest());
			return requestFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace(stderr);
			return null;
		}
	}

	/*
	 * 生产执行sqlmap的bat文件
	 */
	public String genbatFile(String requestFilePath) {
		try {
			String basedir = (String) System.getProperties().get("java.io.tmpdir");
			String configBasedir = burp.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
			if (configBasedir != null && new File(configBasedir).exists()) {
				basedir = configBasedir;
			}
			
			StringBuilder prefixcommand = new StringBuilder();
			prefixcommand.append("cd "+basedir+System.lineSeparator());
			if (Utils.isWindows()) {
				String diskString = basedir.split(":")[0];
				prefixcommand.append(diskString+":"+System.lineSeparator());
			}
			
			String pythonPath = burp.tableModel.getConfigValueByKey("SQLMap-Python-Path");
			String sqlmapPath = burp.tableModel.getConfigValueByKey("SQLMap-SQLMap.py-Path");
			StringBuilder command = new StringBuilder();
			if (pythonPath != null && new File(pythonPath).exists()) {
				if (new File(pythonPath).isFile()) {
					command.append(pythonPath);
				}else {
					command.append(new File(pythonPath,"python").toString());
				}
			}
			command.append(" ");
			if (sqlmapPath != null && new File(sqlmapPath).exists()) {
				if (new File(sqlmapPath).isFile()) {
					command.append(sqlmapPath);
				}else {
					command.append(new File(sqlmapPath,"sqlmap.py").toString());
				}
			}else {
				command.append("sqlmap.py");
			}
			
			command.append(" -r "+requestFilePath);
			String sqlmapOptions = burp.tableModel.getConfigValueByKey("SQLMap-Options");
			if (sqlmapOptions != null && !sqlmapOptions.equals("")) {
				command.append(" "+sqlmapOptions);
			}
			
			//将命令写入剪切板
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection selection = new StringSelection(command.toString());
			clipboard.setContents(selection, null);
			
			File batFile = new File(basedir,"sqlmap-latest-command.bat");
			if (!batFile.exists()) {
			    batFile.createNewFile();
			}
			
			prefixcommand.append(command);
			
			FileUtils.writeByteArrayToFile(batFile, prefixcommand.toString().getBytes());
			return batFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace(stderr);
			return null;
		}
	}
	
	@Deprecated //设想控制history的方法失败
	public String genPowershellHistory(){
		try {
			String modelString = "#TYPE Microsoft.PowerShell.Commands.HistoryInfo\r\n" + 
					"\"Id\",\"CommandLine\",\"ExecutionStatus\",\"StartExecutionTime\",\"EndExecutionTime\"\r\n" + 
					"\"1\",\"Get-History | Export-Csv -Path c:\\tmpPowershellHistory.csv\",\"Completed\",\"2019/7/2 19:02:46\",\"2019/7/2 19:02:46\"\r\n";
			String recordString  = "\"2\",\"test cmd\",\"Completed\",\"2019/7/2 19:02:46\",\"2019/7/2 19:02:46\"";
			
			String basedir = (String) System.getProperties().get("java.io.tmpdir");
			String configBasedir = burp.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
			if (configBasedir != null && new File(configBasedir).exists()) {
				basedir = configBasedir;
			}
			
			File batFile = new File(basedir,"tmpPowershellHistory.csv");
			if (!batFile.exists()) {
			    batFile.createNewFile();
			}
			String content = modelString+recordString;
			FileUtils.writeByteArrayToFile(batFile, content.toString().getBytes());
			return batFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace(stderr);
			return null;
		}
	}
	
	/*
	 * 执行bat文件的命令，为什么需要bat文件？使用了bat文件执行命令，Ctrl+C才不会退出命令行终端
	 */
	public static String SQLMapCommand(String batfilepath) {
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
		return command;
	}
	
	public static void main(String[] args) {
		try {
			Process process = Runtime.getRuntime().exec(SQLMapCommand("ping www.baidu.com"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
