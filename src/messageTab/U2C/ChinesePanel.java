package messageTab.U2C;

import java.awt.*;
import java.awt.event.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
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
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import static org.fife.ui.rsyntaxtextarea.RSyntaxUtilities.getWordEnd;
import static org.fife.ui.rsyntaxtextarea.RSyntaxUtilities.getWordStart;

public class ChinesePanel extends JPanel {

    private final JButton buttonChangeEncoding;
    private final RSyntaxTextArea textArea;
    private final JTextField searchField;
    boolean isRequest;
    private final ChineseTab chineseTab;

    ChinesePanel(ChineseTab chineseTab) {

        this.chineseTab = chineseTab;
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout(0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonChangeEncoding = new JButton("Change Encoding");
        buttonChangeEncoding.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                display(ChinesePanel.this.chineseTab.getOriginContent(), isRequest, ChinesePanel.this.chineseTab.getNextCharSet());
            }
        });
        buttonPanel.add(buttonChangeEncoding);
        add(buttonPanel, BorderLayout.NORTH);

        textArea = new RSyntaxTextArea();
        // 添加鼠标监听器来处理双击事件
        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) { // 判断是否双击事件
                    try {
                        int caretPosition = textArea.viewToModel(e.getPoint()); // 获取鼠标点击的位置对应的文本位置
                        int start = getWordStart(textArea, caretPosition);
                        int end = getWordEnd(textArea, caretPosition);
                        textArea.setSelectionStart(start); // 设置选中文本的起始位置
                        textArea.setSelectionEnd(end); // 设置选中文本的结束位置
                    } catch (BadLocationException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
        textArea.setAutoscrolls(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        //createShortCut();

        // 创建 RTextScrollPane，用于支持滚动
        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        // 添加行号
        scrollPane.setLineNumbersEnabled(true);
        // 设置 RTextScrollPane 的滚动条策略
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

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
        add(searchField, BorderLayout.SOUTH);

    }

    /**
     *
     */
    private void createShortCut() {
        // 创建 ActionMap 和 InputMap
        ActionMap actionMap = textArea.getActionMap();
        InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);

        // 自定义快捷键
        KeyStroke copyKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK);
        KeyStroke cutKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK);

        // 绑定快捷键到对应的 Action
        inputMap.put(copyKeyStroke, "copy");
        actionMap.put("copy", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.copy();
            }
        });

        inputMap.put(pasteKeyStroke, "paste");
        actionMap.put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.paste();
            }
        });

        inputMap.put(cutKeyStroke, "cut");
        actionMap.put("cut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.cut();
            }
        });
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
    public void display(byte[] content, boolean isRequest, String currentCharset) {
        HelperPlus getter = BurpExtender.getHelperPlus();

        try {
            byte[] newContent = handleContent(content, isRequest, currentCharset);

            if (getter.isJSON(content, isRequest)) {
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
            } else {
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
            }
            try {
                textArea.setText(new String(newContent, currentCharset));
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
        }
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
                    contentStr = StringEscapeUtils.unescapeJava(contentStr);
                    i++;
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
