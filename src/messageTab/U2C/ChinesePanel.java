package messageTab.U2C;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.apache.commons.text.StringEscapeUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.JsonUtils;
import com.bit4woo.utilbox.utils.TextUtils;

import burp.BurpExtender;

public class ChinesePanel extends JPanel {

	private final JButton buttonChangeEncoding;
	private final RSyntaxTextArea textArea;
	private final JTextField searchField;

	byte[] content;
	boolean isRequest;
	private ChineseTab chineseTab;


	ChinesePanel(ChineseTab chineseTab){

		this.chineseTab = chineseTab;
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));

		buttonChangeEncoding = new JButton("Change Encoding");
		buttonChangeEncoding.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				display(content,isRequest,chineseTab.getNextCharSet());
			}
		});
		add(buttonChangeEncoding, BorderLayout.NORTH);

		textArea = new RSyntaxTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		add(textArea, BorderLayout.CENTER);

		searchField = new JTextField();
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				search();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				search();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				search();
			}
		});
		add(textArea,BorderLayout.SOUTH);

	}

	private void search() {
		Highlighter highlighter = textArea.getHighlighter();
		highlighter.removeAllHighlights();

		String searchTerm = searchField.getText();
		if (searchTerm.isEmpty()) {
			return;
		}

		String text = textArea.getText();
		Pattern pattern = Pattern.compile(Pattern.quote(searchTerm));
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			try {
				highlighter.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}


	/**
	 * 使用特定编码显示内容,变化原始编码。
	 */
	public void display(byte[] content, boolean isRequest,String currentCharset) {
		try {

			byte[] newcontent = handleContent(content,isRequest,currentCharset);
			textArea.setText(new String(newcontent,currentCharset));

			String text = String.format("Change Encoding: (Using %s)", currentCharset);
			buttonChangeEncoding.setText(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 如果有Unicode编码的内容，就进行escape操作，否则内容和原始内容一致。
	 * @param content
	 * @param isRequest
	 * @return
	 */
	public static byte[] handleContent(byte[] content,boolean isRequest,String charSet) {

		byte[] displayContent = content;
		HelperPlus getter = BurpExtender.getHelperPlus();

		try {

			String contentStr = new String(content,charSet);
			if (getter.isJSON(content,isRequest)) {
				////先尝试进行JSON格式的美化，如果其中有Unicode编码也会自动完成转换
				try {
					byte[] body = HelperPlus.getBody(isRequest, content);
					byte[] newBody = JsonUtils.pretty(new String(body,charSet)).getBytes(charSet);

					displayContent = getter.UpdateBody(isRequest,displayContent,newBody);
					return displayContent;//如果JSON美化成功，主动返回。
				}catch(Exception e) {

				}
			}else {
				int i=0;
				while (TextUtils.needUnicodeConvert(contentStr) && i<3){
					contentStr = StringEscapeUtils.unescapeJava(contentStr);
					i++;
				}

				displayContent = contentStr.getBytes(charSet);
			}

		} catch (Exception e1) {

		}
		return displayContent;
	}
}
