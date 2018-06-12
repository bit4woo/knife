package U2C;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

public class GUI2 {

	public JFrame frame;
	public String ExtenderName = "U2C v0.4 by bit4";
	public String github = "https://github.com/bit4woo/U2C";
	public JLabel lblNewLabel_1;
	public JCheckBox chckbx_proxy;
	public JCheckBox chckbx_repeater;
	public JCheckBox chckbx_intruder;
	private JPanel content_panel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI2 window = new GUI2();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI2() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 832, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		
		content_panel = new JPanel();
		content_panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		content_panel.setLayout(new BorderLayout(0, 0));
		
		frame.getContentPane().add(content_panel, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		content_panel.add(panel, BorderLayout.NORTH);
		FlowLayout fl_panel = (FlowLayout) panel.getLayout();
		fl_panel.setAlignment(FlowLayout.LEFT);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
	}

}
