package messageTab.Info;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.text.StringEscapeUtils;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import burp.IRequestInfo;
import burp.IResponseInfo;

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

	private static IExtensionHelpers helpers;

	public InfoTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		panel = createpanel();
		InfoTab.helpers = helpers;
	}


	public JPanel createpanel() {

		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));

		return contentPane;
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

	@Override
	public void setMessage(byte[] content, boolean isRequest)
	{
		originContent = content;
		
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

	@Override
	public byte[] getSelectedData()
	{
		return null;//TODO
	}


	public static void main(String[] args) {
	}
}