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
import burp.RobotInput;
import burp.TerminalExec;
import burp.Utils;


public class RunSQLMapMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public RunSQLMapMenu(BurpExtender burp){
		this.setText("^_^ Run SQLMap");
		this.addActionListener(new RunSQLMap_Action(burp,burp.invocation));
	}
}

class RunSQLMap_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;
	public String workdir;

	public RunSQLMap_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = burp.callbacks;
		this.stderr = burp.stderr;
		this.stdout = burp.stdout;
		workdir = (String) System.getProperties().get("java.io.tmpdir");
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

						boolean useRobot = (BurpExtender.tableModel.getConfigValueByKey("RunTerminalWithRobotInput") != null);
						if (useRobot) {
							RobotInput.startCmdConsole();//尽早启动减少出错概率
						}

						IHttpRequestResponse message = messages[0];
						String requestFilePath = RequestToFile(message);
						String pythonPath = burp.tableModel.getConfigValueByKey("SQLMap-Python-Path");
						String sqlmapPath = burp.tableModel.getConfigValueByKey("SQLMap-SQLMap.py-Path");
						String sqlmapOptions = burp.tableModel.getConfigValueByKey("SQLMap-Options");

						if (pythonPath ==null || pythonPath.trim().equals("")) {
							pythonPath = "python.exe";
						}

						if (sqlmapPath ==null || sqlmapPath.trim().equals("")) {
							sqlmapPath = "sqlmap.py";
						}

						String paras = " -r "+requestFilePath;
						if (sqlmapOptions != null) {
							paras = paras+" "+sqlmapOptions;
						}

						if (useRobot) {
							//方案1：使用模拟输入实现
							//RobotInput.startCmdConsole();//尽早启动减少出错概率
							String sqlmapCmd = RobotInput.genCmd(pythonPath, sqlmapPath, paras);
							ri.inputString(sqlmapCmd);
						}else {
							//方案2：使用bat文件实现
							TerminalExec xxx = new TerminalExec(workdir,"sqlmap-knife.bat",pythonPath, sqlmapPath, paras);
							xxx.run();
						}
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

			String configBasedir = burp.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
			if (configBasedir != null && new File(configBasedir).exists()) {
				workdir = configBasedir;
			}

			File requestFile = new File(workdir,filename);
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
	public String changeDirCommand(){
		//运行命令的工作目录，work path
		String basedir = (String) System.getProperties().get("java.io.tmpdir");
		String configBasedir = burp.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
		if (configBasedir != null && new File(configBasedir).exists()) {
			basedir = configBasedir;
		}
		String command = "cd "+basedir+System.lineSeparator();

		if (Utils.isWindows()) {//如果是windows，还要注意不同磁盘的切换
			String diskString = basedir.split(":")[0];
			command =command+ diskString+":"+System.lineSeparator();
		}
		return command;
	}

	public String genSqlmapCmd(String requestFilePath) {

		String pythonPath = burp.tableModel.getConfigValueByKey("SQLMap-Python-Path");
		String sqlmapPath = burp.tableModel.getConfigValueByKey("SQLMap-SQLMap.py-Path");
		StringBuilder command = new StringBuilder();

		command.append(changeDirCommand());

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
