package U2C;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import burp.BurpExtender;
import burp.HttpMessageCharSet;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import burp.IMessageEditorTabFactory;
import burp.ITextEditor;

public class ChineseTabFactory implements IMessageEditorTabFactory
{
	private static IExtensionHelpers helpers;
	private static IBurpExtenderCallbacks callbacks;

	public static final String majorVersion = BurpExtender.callbacks.getBurpVersion()[1].replaceAll("[a-zA-Z]","");
	public static final String minorVersion = BurpExtender.callbacks.getBurpVersion()[2].replaceAll("[a-zA-Z]","");//18beta

	//stdout.println(majorVersion+"   "+minorVersion);
	//2020.2.1 ==>2020   2.1
	//2.1.06 ==> 2.1   06

	public static boolean needJSON() {
		try {
			float majorV = Float.parseFloat(majorVersion);
			float minorV = Float.parseFloat(minorVersion);
			if (majorV>=2020 && minorV >= 4.0f) { //2020.4及之后已经有了JSON美化的功能，不再需要
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace(BurpExtender.getStderr());
			return true;
		}
	}


	public ChineseTabFactory(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		ChineseTabFactory.callbacks = callbacks;
		ChineseTabFactory.helpers = helpers;
	}

	@Override
	public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
		return new ChineseTab(controller,editable,helpers,callbacks);
	}
}

class ChineseTab implements IMessageEditorTab{
	private ITextEditor txtInput;
	private JPanel panel;
	private byte[] originContent;
	private String originalCharSet;
	private byte[] displayContent = "Nothing to show".getBytes();
	private static final String[] encoding = {"UTF-8","gbk","gb2312","GB18030","Big5","Unicode"};
	private List<String> encodingList = Arrays.asList(encoding);
	private String currentCharSet = encodingList.get(0);
	JButton btnNewButton;
	private int charSetIndex;
	public ChineseTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		txtInput = callbacks.createTextEditor();
		panel = createpanel();
		txtInput.setEditable(editable);
	}

	public JPanel createpanel() {


		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));

		btnNewButton = new JButton("Change Encoding");
		contentPane.add(btnNewButton, BorderLayout.NORTH);

		contentPane.add(txtInput.getComponent(), BorderLayout.CENTER);

		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentCharSet = nextCharSet();
				display(currentCharSet);
			}
		});

		return contentPane;
	}

	@Override
	public String getTabCaption()
	{
		return "Chinese";
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
		if(content==null) {
			txtInput.setText(displayContent);
			return;
		}

		originContent = content;
		originalCharSet = HttpMessageCharSet.getCharset(content);

		display(currentCharSet);
	}

	public void display(String currentCharSet) {
		try {

			String originalString = new String(originContent,originalCharSet);
			txtInput.setText(originalString.getBytes(currentCharSet));

			String text = String.format("Change Encoding: (Using %s)", currentCharSet);
			btnNewButton.setText(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String nextCharSet() {
		if (charSetIndex < encodingList.size()-1) {
			charSetIndex++;
		}else {
			charSetIndex =0;
		}
		return encodingList.get(charSetIndex);
	}

	/**
	 * 中文下的编辑还是有问题，暂不支持。
	 */
	@Override
	public byte[] getMessage()
	{
		/*
		try {
			byte[] text = txtInput.getText();//这个时候应该是当前编码对应的byte[]
			byte[] result = new String(text,currentCharSet).getBytes(originalCharSet);
			return result;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return originContent;
		}*/
		return originContent;
	}

	@Override
	public boolean isModified()
	{
		return txtInput.isTextModified();
	}

	@Override
	public byte[] getSelectedData()
	{
		return txtInput.getSelectedText();
	}

	public static void main(String args[]) {
	}
}