package knife;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

public class ChineseGUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ChineseGUI frame = new ChineseGUI("中文".getBytes());
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ChineseGUI(byte[] body) {
		
		List<String> encodingList = new ArrayList<String>();
		encodingList.add("UTF-8");
		encodingList.add("gbk");
		encodingList.add("gb2312");
		encodingList.add("GB18030");
		encodingList.add("Big5");
		encodingList.add("Unicode");
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		this.setTitle("View Chinese");
		
		JButton btnNewButton = new JButton("Change Encoding");
		contentPane.add(btnNewButton, BorderLayout.NORTH);
		
		JLabel lblCoding = new JLabel("Encoding: ");
		contentPane.add(lblCoding, BorderLayout.SOUTH);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		scrollPane.setViewportView(textArea);
		
		btnNewButton.addActionListener(new ActionListener() {
			int i =0;
			public void actionPerformed(ActionEvent e) {
				try {
					String encoding = encodingList.get(i);
					lblCoding.setText("Encoding: "+encoding);
					int localtion = textArea.getCaretPosition();
					textArea.setText(new String(body,encoding));
					textArea.setCaretPosition(localtion);
					if (i < encodingList.size()-1) {
						i++;
					}else {
						i =0;
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnNewButton.doClick();//首次启动需要显示内容
	}
}
