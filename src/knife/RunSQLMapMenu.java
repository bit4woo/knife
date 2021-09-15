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
import config.GUI;


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
						String pythonPath = GUI.tableModel.getConfigValueByKey("SQLMap-Python-Path");
						String sqlmapPath = GUI.tableModel.getConfigValueByKey("SQLMap-SQLMap.py-Path");
						String sqlmapOptions = GUI.tableModel.getConfigValueByKey("SQLMap-Options");

						if (pythonPath ==null || pythonPath.trim().equals("")) {
							pythonPath = "python";
						}

						if (sqlmapPath ==null || sqlmapPath.trim().equals("")) {
							sqlmapPath = "sqlmap.py";
						}

						String paras = " -r "+requestFilePath;
						if (message.getHttpService().getProtocol().equalsIgnoreCase("https")) {
							paras = paras+" --force-ssl";
						}
						
						if (sqlmapOptions != null) {
							paras = paras+" "+sqlmapOptions;
						}

						String sqlmapCmd = TerminalExec.genCmd(pythonPath, sqlmapPath, paras);
						if (useRobot) {
							//方案1：使用模拟输入实现
							//RobotInput.startCmdConsole();//尽早启动减少出错概率
							ri.inputString(sqlmapCmd);
						}else {
							//方案2：使用bat文件实现
							String file = TerminalExec.genBatchFile(sqlmapCmd, "sqlmap-knife.bat");
							TerminalExec.runBatchFile(file);
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

			String configBasedir = GUI.tableModel.getConfigValueByKey("SQLMap-Request-File-Path");
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

	public static void main(String[] args) {

	}
}
