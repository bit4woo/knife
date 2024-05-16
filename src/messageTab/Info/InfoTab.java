package messageTab.Info;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import com.bit4woo.utilbox.utils.ByteArrayUtils;
import com.bit4woo.utilbox.utils.EmailUtils;
import com.bit4woo.utilbox.utils.TextUtils;
import com.bit4woo.utilbox.utils.UrlUtils;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import knife.FindUrlAndRequest;

/** 
 * @author bit4woo
 * @github https://github.com/bit4woo 
 * @version CreateTime：2022年1月15日 下午11:07:59 
 * 
 * 想要正确显示中文内容，有三个编码设置会影响结果：
 * 1、原始编码，通过代码尝试自动获取，但是结果可能不准确，极端情况下需要手动设置。
 * 2、转换后的编码，手动设置。
 * 3、burp设置的显示编码，显示时时用的编码，应该和转换后的编码一致。
 * 
 * 原始数据是byte[],但也是文本内容的某种编码的byte[].
 * 
 */
public class InfoTab implements IMessageEditorTab{
	private JPanel panel;

	private byte[] originContent;

	public InfoTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		panel = new InfoPanel(this);
		BurpExtender.getCallbacks().customizeUiComponent(panel);//尝试使用burp的font size
	}

	@Override
	public String getTabCaption()
	{
		return "Info";
	}

	@Override
	public Component getUiComponent()
	{
		return panel;
	}

	@Override
	public boolean isEnabled(byte[] content, boolean isRequest)
	{
		return true;
	}

	/**
	 * 每次切换到这个tab，都会调用这个函数。应考虑避免重复劳动，根据originContent是否变化来判断。
	 */
	@Override
	public void setMessage(byte[] content, boolean isRequest){	
		if (content ==null || content.length ==0) {
			return;
		}else if (ByteArrayUtils.equals(originContent,content)) {
			return;
		}else {
			originContent = content;
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					String text = new String(content);
					text = TextUtils.decodeAll(text);

					List<String> urls = UrlUtils.grepUrlsWithProtocol(text);
					urls.addAll(UrlUtils.grepUrlsInQuotes(text));
					urls.addAll(UrlUtils.grepUrlPathNotStartWithSlashInQuotes(text));
					urls = FindUrlAndRequest.cleanUrls(urls);
					for (String url:urls) {
						InfoEntry aaa = new InfoEntry(url,InfoEntry.Type_URL);
						((InfoPanel)panel).getTable().getInfoTableModel().addNewInfoEntry(aaa);
					}

					List<String> emails = EmailUtils.grepEmail(text);
					for (String email:emails) {
						InfoEntry aaa = new InfoEntry(email,InfoEntry.Type_Email);
						((InfoPanel)panel).getTable().getInfoTableModel().addNewInfoEntry(aaa);
					}

					return null;
				}
			};
			worker.execute();
		}
	}


	/**
	 * 中文下的编辑还是有问题，暂不支持。
	 * 始终返回原始内容。
	 */
	@Override
	public byte[] getMessage()
	{
		return originContent;
	}

	@Override
	public boolean isModified()
	{
		return false;
	}
	
	/**
	 * ctrl+c复制数据逻辑会调用这个函数
	 */
	@Override
	public byte[] getSelectedData()
	{	
		JTable table = ((InfoPanel)panel).getTable();
		int[] rows = table.getSelectedRows();
		int[] columns = table.getSelectedColumns();
		List<String> result = new ArrayList<>();
		for (int row:rows) {
			List<String> line = new ArrayList<>();
			for (int column:columns) {
				try {
					String value = (String)table.getValueAt(row, column);
					line.add(value);
				} catch (Exception e) {
					//e.printStackTrace();
				}
			}
			result.add(String.join(" ", line));
		}
		return String.join(" ", result).getBytes();
	}


	public static void main(String[] args) {
	}
}