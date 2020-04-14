package knife;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
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
import burp.RobotInput;
import burp.Utils;


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
					RobotInput ri = new RobotInput();
					IHttpRequestResponse[] messages = invocation.getSelectedMessages();
					if (messages !=null) {
						IHttpRequestResponse message = messages[0];
						String requestFilePath = RequestToFile(message);
						RobotInput.startCmdConsole();
						changeDir();
						String sqlmapCmd = genSqlmapCmd(requestFilePath);
						ri.inputString(sqlmapCmd);
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
	 * 切换工作目录
	 */
	public void changeDir() throws AWTException {
		//运行命令的工作目录，work path
		String basedir = (String) System.getProperties().get("java.io.tmpdir");
		String configBasedir = burp.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
		if (configBasedir != null && new File(configBasedir).exists()) {
			basedir = configBasedir;
		}
		String command = "cd "+basedir+System.lineSeparator();

		RobotInput ri = new RobotInput();
		ri.inputString(command.toString()); //切换目录

		if (Utils.isWindows()) {//如果是windows，还要注意不同磁盘的切换
			String diskString = basedir.split(":")[0];
			ri.inputString(diskString+":"+System.lineSeparator());
		}
	}

	public String genSqlmapCmd(String requestFilePath) {

		String pythonPath = burp.tableModel.getConfigValueByKey("SQLMap-Python-Path");
		String sqlmapPath = burp.tableModel.getConfigValueByKey("SQLMap-SQLMap.py-Path");
		StringBuilder command = new StringBuilder();
		if (pythonPath != null && new File(pythonPath).exists()) {
			if (new File(pythonPath).isFile()) {
				command.append(pythonPath);
			}else {
				command.append(new File(pythonPath,"python").toString());
			}
			command.append(" ");
		}
		
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
		command.append(System.lineSeparator());
		return command.toString();
	}


	public static void main(String[] args) {

	}
}
