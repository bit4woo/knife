package messageTab.Info;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class InfoPanel extends JPanel {

    private final JTextField searchField;
    private final JLabel statusLabel = new JLabel("   0 matches");
    boolean isRequest;
    
    InfoTable table;
	private InfoTab InfoTab;

	
    public InfoTab getInfoTab() {
		return InfoTab;
	}


	public void setInfoTab(InfoTab infoTab) {
		InfoTab = infoTab;
	}


	public InfoTable getTable() {
		return table;
	}


	public void setTable(InfoTable table) {
		this.table = table;
	}


	InfoPanel(InfoTab parent) {
		this.InfoTab = parent;
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(new BorderLayout(0, 0));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(buttonPanel, BorderLayout.NORTH);

        InfoTableModel model = new InfoTableModel();
        table = new InfoTable(model,this);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(table);
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
            }
        });
        JButton rightButton = new JButton(">");
        rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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


    private Timer createSearchTimer() {
        Timer searchTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 执行搜索操作
                String searchTerm = searchField.getText();
                //search(searchTerm, false, false);
            }
        });
        searchTimer.setRepeats(false); // 设置计时器只执行一次
        return searchTimer;
    }
    /*
    private void search(String searchTerm, boolean isRegex, boolean isCaseSensitive) {
        if (searchTerm.isEmpty()) {
            return;
        }

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
    }*/



    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame jf = new JFrame();
                InfoPanel panel = new InfoPanel(null);
                jf.setContentPane(panel);
                jf.setVisible(true);
                jf.pack();
            }
        });
    }
}
