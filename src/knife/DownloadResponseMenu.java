package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import org.apache.commons.io.FileUtils;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IParameter;


public class DownloadResponseMenu extends JMenuItem {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//JMenuItem vs. JMenu
	public DownloadResponseMenu(BurpExtender burp){
		this.setText("^_^ Download Response");
		this.addActionListener(new Download_Action(burp,burp.invocation));
	}
}

class Download_Action implements ActionListener{
	private IContextMenuInvocation invocation;
	public IExtensionHelpers helpers;
	public PrintWriter stdout;
	public PrintWriter stderr;
	public IBurpExtenderCallbacks callbacks;
	public BurpExtender burp;

	public Download_Action(BurpExtender burp,IContextMenuInvocation invocation) {
		this.burp = burp;
		this.invocation  = invocation;
		this.helpers = burp.helpers;
		this.callbacks = BurpExtender.callbacks;
		this.stderr = BurpExtender.stderr;
		this.stdout = BurpExtender.stdout;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		Runnable SqlmapRunner = new Runnable() {
			@Override
			public void run() {
				try{
					IHttpRequestResponse[] messages = invocation.getSelectedMessages();
					Getter getter = new Getter(helpers);
					if (messages == null) {
						return;
					}
					if (messages.length == 1) {

						String filename = getter.getFullURL(messages[0]).getFile();
						byte[] respBody = getter.getBody(false, messages[0]);
						File downloadFile = saveDialog(filename);
						if (downloadFile!= null) {
							FileUtils.writeByteArrayToFile(downloadFile, respBody);
						}
					}else {
						File rootPath = selectPath();//指定多个文件保存的根目录
						//						System.out.println("rootPath:"+rootPath);

						for (IHttpRequestResponse message:messages) {
							try {
								byte[] respBody = getter.getBody(false, message);
								File fullName = getFileName(message,rootPath);
								System.out.println("Save file: "+fullName);
								if (fullName!= null) {
									//System.out.println(fullName);
									FileUtils.writeByteArrayToFile(fullName, respBody);
								}
							} catch (Exception e) {
								e.printStackTrace(BurpExtender.getStderr());
							}
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

	public File saveDialog(String defaultFileName) {
		try {
			JFileChooser fc =  new JFileChooser();
			if (fc.getCurrentDirectory() != null) {
				fc = new JFileChooser(fc.getCurrentDirectory());
			}else {
				fc = new JFileChooser();
			}

			fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
			fc.setSelectedFile(new File(defaultFileName));

			int action = fc.showSaveDialog(null);

			if(action==JFileChooser.APPROVE_OPTION){
				File file=fc.getSelectedFile();
				return file;
			}
			return null;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public File selectPath() {
		try {
			JFileChooser fc =  new JFileChooser();
			if (fc.getCurrentDirectory() != null) {
				fc = new JFileChooser(fc.getCurrentDirectory());
			}else {
				fc = new JFileChooser();
			}

			fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int action = fc.showSaveDialog(null);

			if(action==JFileChooser.APPROVE_OPTION){
				File path=fc.getSelectedFile();
				return path;
			}
			return null;
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}


	public File getFileName(IHttpRequestResponse message,File rootPath) throws IOException{
		String canonicalFile = "KnifeSaved";
		try {
			Getter getter = new Getter(helpers);

			String pathStr = null;
			//1、从参数名中获取文件名称，任意文件读取多是这种情况
			List<IParameter> paras = getter.getParas(message);
			for (IParameter para:paras) {
				if (para.getType() == IParameter.PARAM_COOKIE) continue;
				String value = para.getValue();
				int num = value.length()-value.replaceAll("/", "").length();
				if (num >=2) {
					pathStr = value;
					break;
				}
			}

			for (IParameter para:paras) {
				if (para.getType() == IParameter.PARAM_COOKIE) continue;
				String value = para.getValue();
				int num = value.length()-value.replaceAll("\\\\", "").length();//是正则表达式
				if (num >=2) {
					pathStr = value;
					break;
				}
			}

			//2、使用url Path作为文件名，
			if (null == pathStr) {
				pathStr = getter.getFullURL(message).getPath();//getFile()包含了query中的内容
				pathStr  = pathStr.substring(pathStr.lastIndexOf("/"));
			}

			canonicalFile = new File(pathStr).getCanonicalFile().toString();
			//System.out.println("canonicalFile: "+canonicalFile);
			canonicalFile = canonicalFile.substring(canonicalFile.indexOf(File.separator));//如果是windows系统，需要去除磁盘符号
		} catch (IOException e) {
			e.printStackTrace();
		}

		File fullName = new File(rootPath,canonicalFile);
		//System.out.println("fullName: "+fullName);

		if (fullName.exists()){
			SimpleDateFormat simpleDateFormat = 
					new SimpleDateFormat("YYMMdd-HHmmss");
			String timeString = simpleDateFormat.format(new Date());
			fullName = new File(rootPath,canonicalFile+timeString);
		}
		return fullName;
	}

	public static void main(String[] args) {

	}
}
