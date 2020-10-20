package knife;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;

import org.apache.commons.io.FileUtils;

import burp.BurpExtender;
import burp.Getter;
import burp.IBurpExtenderCallbacks;
import burp.IContextMenuInvocation;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;


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
					if (messages !=null) {
						IHttpRequestResponse message = messages[0];
						SaveToFile(message);
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
	public void SaveToFile(IHttpRequestResponse message) {
		try {
			Getter getter = new Getter(helpers);
			byte[] respBody = getter.getBody(false, message);
			String filename = getter.getFullURL(message).getFile();

			File downloadFile = saveDialog(filename);
			if (downloadFile!= null) {
				FileUtils.writeByteArrayToFile(downloadFile, respBody);
			}
		} catch (IOException e) {
			e.printStackTrace(stderr);
		}
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

	public static void main(String[] args) {

	}
}
