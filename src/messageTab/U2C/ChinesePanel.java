package messageTab.U2C;

import static org.fife.ui.rsyntaxtextarea.RSyntaxUtilities.getWordEnd;
import static org.fife.ui.rsyntaxtextarea.RSyntaxUtilities.getWordStart;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.apache.commons.text.StringEscapeUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.JsonUtils;
import com.bit4woo.utilbox.utils.TextUtils;

import burp.BurpExtender;
import burp.ITextEditor;

public class ChinesePanel extends JPanel {

	private final JButton buttonChangeEncoding;
	private final JButton buttonPreviousPage;
	private final JButton buttonnextPage;
	private final RSyntaxTextArea textArea;
	private final JTextField searchField;
	private final JLabel statusLabel = new JLabel("   0 matches");
	boolean isRequest;
	private final ChineseTab chineseTab;

	private int currentHighlightIndex = 0;
	private int currentPage = 1;
	private final JLabel currentPageLabel = new JLabel(" "+ currentPage+" ");
	private int maxPage;

	ChinesePanel(ChineseTab chineseTab) {

		this.chineseTab = chineseTab;
		setBorder(new EmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout(0, 0));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		buttonChangeEncoding = new JButton("Change Encoding");
		buttonChangeEncoding.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentPage = 1;
				displayCurrentPage();
				displayInChunks(ChinesePanel.this.chineseTab.getOriginContent(), isRequest, ChinesePanel.this.chineseTab.getNextCharSet(),currentPage);
			}
		});


		buttonPreviousPage = new JButton("Previous Page");
		buttonPreviousPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentPage = currentPage -1;
				if (currentPage <=1) {
					currentPage =1;
				}
				displayCurrentPage();
				displayInChunks(ChinesePanel.this.chineseTab.getOriginContent(), isRequest, ChinesePanel.this.chineseTab.getCurrentCharSet(),currentPage);
			}
		});

		buttonnextPage = new JButton("Next Page");
		buttonnextPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentPage = currentPage +1;
				if (currentPage > maxPage) {
					currentPage =maxPage;
				}
				displayCurrentPage();
				displayInChunks(ChinesePanel.this.chineseTab.getOriginContent(), isRequest, ChinesePanel.this.chineseTab.getCurrentCharSet(),currentPage);
			}
		});


		buttonPanel.add(buttonChangeEncoding);
		buttonPanel.add(buttonPreviousPage);
		buttonPanel.add(currentPageLabel);
		buttonPanel.add(buttonnextPage);


		add(buttonPanel, BorderLayout.NORTH);

		textArea = createRSyntaxTextArea();
		// 创建 RTextScrollPane，用于支持滚动
		RTextScrollPane scrollPane = new RTextScrollPane(textArea);
		// 添加行号
		scrollPane.setLineNumbersEnabled(true);
		// 设置 RTextScrollPane 的滚动条策略
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		add(scrollPane, BorderLayout.CENTER);


		JPanel footPanel = new JPanel(new BorderLayout());
		searchField = new JTextField();
		Timer searchTimer = createSearchTimer();
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				searchTimer.restart();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				searchTimer.restart();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				searchTimer.restart();
			}
		});

		JButton leftButton = new JButton("<");
		leftButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Highlighter.Highlight[] highLights = textArea.getHighlighter().getHighlights();
				scrollToHighLigth(currentHighlightIndex--);
				if (currentHighlightIndex < 0) {
					currentHighlightIndex = highLights.length - 1;
				}
			}
		});
		JButton rightButton = new JButton(">");
		rightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Highlighter.Highlight[] highLights = textArea.getHighlighter().getHighlights();
				scrollToHighLigth(currentHighlightIndex++);
				if (currentHighlightIndex >= highLights.length) {
					currentHighlightIndex = 0;
				}
			}
		});
		JPanel panelA = new JPanel();
		panelA.add(leftButton);
		panelA.add(rightButton);

		footPanel.add(panelA, BorderLayout.WEST);
		footPanel.add(searchField, BorderLayout.CENTER);

		footPanel.add(statusLabel, BorderLayout.EAST);

		add(footPanel, BorderLayout.SOUTH);
	}


	public void displayCurrentPage() {
		currentPageLabel.setText(" "+currentPage+" ");
	}
	public RSyntaxTextArea createRSyntaxTextArea() {
		RSyntaxTextArea rstextArea = new RSyntaxTextArea();
		// 添加鼠标监听器来处理双击事件
		rstextArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) { // 判断是否双击事件
					try {
						int caretPosition = rstextArea.viewToModel(e.getPoint()); // 获取鼠标点击的位置对应的文本位置
						int start = getWordStart(rstextArea, caretPosition);
						int end = getWordEnd(rstextArea, caretPosition);
						rstextArea.setSelectionStart(start); // 设置选中文本的起始位置
						rstextArea.setSelectionEnd(end); // 设置选中文本的结束位置
					} catch (BadLocationException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		});
		rstextArea.setAutoscrolls(true);
		rstextArea.setLineWrap(true);
		rstextArea.setWrapStyleWord(true);
		rstextArea.setEditable(false);
		//createShortCut();
		return rstextArea;
	}

	public ITextEditor createITextEditor() {
		return BurpExtender.getCallbacks().createTextEditor();
	}

	private void scrollToHighLigth(int index) {
		Highlighter.Highlight[] highLights = textArea.getHighlighter().getHighlights();
		if (index >= highLights.length || index < 0) {
			return;
		}
		try {
			int end = highLights[index].getEndOffset();
			Rectangle rect = textArea.modelToView(end); // 获取中心位置的矩形范围
			textArea.scrollRectToVisible(rect); // 将该矩形范围滚动至可见
			textArea.setCaretPosition(end);
		} catch (BadLocationException e1) {
			e1.printStackTrace();
		}
	}

	private Timer createSearchTimer() {
		Timer searchTimer = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// 执行搜索操作
				String searchTerm = searchField.getText();
				search(searchTerm, false, false);
			}
		});
		searchTimer.setRepeats(false); // 设置计时器只执行一次
		return searchTimer;
	}

	private void search(String searchTerm, boolean isRegex, boolean isCaseSensitive) {
		if (searchTerm.isEmpty()) {
			return;
		}

		// 清除之前的高亮显示
		Highlighter highlighter = textArea.getHighlighter();
		highlighter.removeAllHighlights();

		String text = textArea.getText();
		int flags = 0;
		if (!isCaseSensitive) {
			flags |= Pattern.CASE_INSENSITIVE;
		}

		Pattern pattern;
		if (isRegex) {
			pattern = Pattern.compile(searchTerm, flags);
			Matcher matcher = pattern.matcher(text);
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				try {
					highlighter.addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		} else {
			int index = text.indexOf(searchTerm);
			while (index != -1) {
				try {
					textArea.getHighlighter().addHighlight(index, index + searchTerm.length(), new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
					index = text.indexOf(searchTerm, index + searchTerm.length()); // 继续搜索下一个匹配项
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		}
		int num = textArea.getHighlighter().getHighlights().length;
		statusLabel.setText("   " + num + " matches");
	}


	/**
	 * 使用特定编码显示内容,变化原始编码。
	 */
	@Deprecated
	public void display(byte[] content, boolean isRequest, String currentCharset) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				HelperPlus getter = BurpExtender.getHelperPlus();
				int position = textArea.getCaretPosition();

				try {
					byte[] newContent = handleContent(content, isRequest, currentCharset);

					if (getter.isJSON(content, isRequest)) {
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
					} else if (getter.isJavaScript(content, isRequest)) {
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
					} else {
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
					}

					try {
						textArea.setText(new String(newContent, currentCharset));
						textArea.setCaretPosition(position);
					} catch (UnsupportedEncodingException e) {
						textArea.setText(e.getMessage());
					}

					String text = String.format("Change Encoding: (Using %s)", currentCharset);
					buttonChangeEncoding.setText(text);
				} catch (Exception e) {

					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String stackTrace = sw.toString();
					textArea.setText(stackTrace);
					textArea.setCaretPosition(position);
				}
				return null;
			}
		};
		worker.execute();
	}

	public void displayInChunks(byte[] content, boolean isRequest, String currentCharset,int page) {
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {

			@Override
			protected Void doInBackground() {
				HelperPlus getter = BurpExtender.getHelperPlus();
				int position = textArea.getCaretPosition();

				try {
					byte[] newContent = handleContent(content, isRequest, currentCharset);
					if (getter.isJSON(content, isRequest)) {
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
					} else if (getter.isJavaScript(content, isRequest)) {
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
					} else {
						textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
					}


					String newContentStr = new String(newContent, currentCharset);
					int chunkSize = 1024 * 50; // 每页显示5 KB的内容

					// 计算最大页码数
					maxPage = (int) Math.ceil((double) newContentStr.length() / chunkSize);

					int start = (page - 1) * chunkSize;
					int end = Math.min(start + chunkSize, newContentStr.length());

					if (start >= newContentStr.length()) {
						textArea.setText(""); // 如果超出内容长度，显示为空
					} else {
						textArea.setText(newContentStr.substring(start, end));
					}


					setCaretPosition(position);
				} catch (UnsupportedEncodingException e) {
					textArea.setText(e.getMessage());
					setCaretPosition(position);
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					String stackTrace = sw.toString();
					textArea.setText(stackTrace);
					setCaretPosition(position);
				}
				return null;
			}

			@Override
			protected void process(List<String> chunks) {
				for (String chunk : chunks) {
					textArea.append(chunk);
				}
			}

			@Override
			protected void done() {
				buttonChangeEncoding.setText(String.format("Change Encoding: (Using %s)", currentCharset));
			}

			private void setCaretPosition(int position) {
				SwingUtilities.invokeLater(() -> textArea.setCaretPosition(position));
			}
		};
		worker.execute();
	}


	/**
	 * 如果有Unicode编码的内容，就进行escape操作，否则内容和原始内容一致。
	 *
	 * @param content
	 * @param isRequest
	 * @return
	 */
	public static byte[] handleContent(byte[] content, boolean isRequest, String charSet) {

		byte[] displayContent = content;
		HelperPlus getter = BurpExtender.getHelperPlus();

		try {

			String contentStr = new String(content, charSet);
			if (getter.isJSON(content, isRequest)) {
				////先尝试进行JSON格式的美化，如果其中有Unicode编码也会自动完成转换
				try {
					byte[] body = HelperPlus.getBody(isRequest, content);
					byte[] newBody = JsonUtils.pretty(new String(body, charSet)).getBytes(charSet);

					displayContent = getter.UpdateBody(isRequest, displayContent, newBody);
					return displayContent;//如果JSON美化成功，主动返回。
				} catch (Exception e) {

				}
			} else {
				int i = 0;
				while (TextUtils.needUnicodeConvert(contentStr) && i < 3) {
					int oldLength = contentStr.length();
					contentStr = StringEscapeUtils.unescapeJava(contentStr);
					i++;
					int newLength = contentStr.length();
					if (oldLength == newLength) {
						break;
					}
				}

				displayContent = contentStr.getBytes(charSet);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return displayContent;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame jf = new JFrame();
				ChinesePanel panel = new ChinesePanel(null);
				jf.setContentPane(panel);
				jf.setVisible(true);
				panel.textArea.setText("aaaaaaaaaa");
				jf.pack();
			}
		});
	}
}
