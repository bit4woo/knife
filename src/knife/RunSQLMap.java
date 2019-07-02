package knife;

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

public class RunSQLMap extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public RunSQLMap(BurpExtender burp){
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
					new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			String timeString = simpleDateFormat.format(new Date());
			String filename = host+"."+timeString+".req";

			String basedir = (String) System.getProperties().get("java.io.tmpdir");
			String configBasedir = burp.tableModel.getConfigByKey("SQLMap-File-Path");
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
			String configBasedir = burp.tableModel.getConfigByKey("SQLMap-File-Path");
			if (configBasedir != null && new File(configBasedir).exists()) {
				basedir = configBasedir;
			}
			
			StringBuilder command = new StringBuilder();
			command.append("cd "+basedir+System.lineSeparator());
			if (Utils.isWindows()) {
				String diskString = basedir.split(":")[0];
				command.append(diskString+":"+System.lineSeparator());
			}
			
			command.append("sqlmap.py -r "+requestFilePath);
			String sqlmapOptions = burp.tableModel.getConfigByKey("SQLMap-Options");
			if (sqlmapOptions != null && !sqlmapOptions.equals("")) {
				command.append(" "+sqlmapOptions);
			}
			
			
			File batFile = new File(basedir,"sqlmap.bat");
			if (!batFile.exists()) {
			    batFile.createNewFile();
			}
			FileUtils.writeByteArrayToFile(batFile, command.toString().getBytes());
			return batFile.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace(stderr);
			return null;
		}
	}
	
	/*
	 * 执行bat文件的命令，为什么需要bat文件？使用了bat文件执行命令，Ctrl+C才不会退出命令行终端
	 */
	String SQLMapCommand(String batfilepath) {
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
}
