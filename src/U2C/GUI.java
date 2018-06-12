package U2C;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import java.awt.Label;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

public class GUI {

	public JFrame frame;
	public String ExtenderName = "U2C v0.4 by bit4";
	public String github = "https://github.com/bit4woo/U2C";
	public JLabel lblNewLabel_1;
	public JCheckBox chckbx_proxy;
	public JCheckBox chckbx_repeater;
	public JCheckBox chckbx_intruder;
	private JPanel content_panel;
	private JCheckBox chckbx_display;
	private JCheckBox chckbx_scanner;
	private JCheckBox chckbx_scope;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
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
	public GUI() {
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
		
		JLabel lblNewLabel = new JLabel("Enable for : ");
		panel.add(lblNewLabel);
		
		chckbx_proxy = new JCheckBox("Proxy");
		chckbx_proxy.setSelected(true);
		panel.add(chckbx_proxy);
		
		chckbx_repeater = new JCheckBox("Repeater");
		panel.add(chckbx_repeater);
		
		chckbx_intruder = new JCheckBox("Intruder");
		panel.add(chckbx_intruder);
		
		chckbx_scanner = new JCheckBox("Scanner");
		panel.add(chckbx_scanner);
		
		JLabel lblNewLabel_display = new JLabel("|");
		panel.add(lblNewLabel_display);
		
		chckbx_display = new JCheckBox("Only Display Converted Body");
		chckbx_display.setSelected(true);
		panel.add(chckbx_display);
		
		chckbx_scope = new JCheckBox("For Items In Scope");
		chckbx_scope.setSelected(true);
		panel.add(chckbx_scope);
				
		JPanel panel_1 = new JPanel();
		content_panel.add(panel_1, BorderLayout.SOUTH);
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
				
		lblNewLabel_1 = new JLabel("    "+github);
		lblNewLabel_1.setFont(new Font("", Font.BOLD, 12));
		lblNewLabel_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					URI uri = new URI(github);
					Desktop desktop = Desktop.getDesktop();
					if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
						desktop.browse(uri);
					}
				} catch (Exception e2) {
					// TODO: handle exception
					//callbacks.printError(e2.getMessage());
				}
				
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				lblNewLabel_1.setForeground(Color.BLUE);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblNewLabel_1.setForeground(Color.BLACK);
			}
		});
		panel_1.add(lblNewLabel_1);
		
		JPanel panel_2 = new JPanel();
		content_panel.add(panel_2, BorderLayout.CENTER);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{660, 0};
		gbl_panel_2.rowHeights = new int[]{23, 23, 15, 0, 0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		Label label = new Label("To display chinese correctly, you should do the following config: ");
		label.setFont(new Font("Dialog", Font.PLAIN, 14));
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.NORTHWEST;
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		panel_2.add(label, gbc_label);
		Label label1 = new Label("1. User options-->Display-->HTTP Message Display-->Change font, select a font that support chinese,eg:Microsoft Yahei.");
		label1.setFont(new Font("Dialog", Font.PLAIN, 14));
		GridBagConstraints gbc_label1 = new GridBagConstraints();
		gbc_label1.anchor = GridBagConstraints.NORTHWEST;
		gbc_label1.insets = new Insets(0, 0, 5, 0);
		gbc_label1.gridx = 0;
		gbc_label1.gridy = 1;
		panel_2.add(label1, gbc_label1);
		
		JLabel Label2 = new JLabel("2. \"Character Sets\" should be set to \"Use the platorm default\" or \"UTF-8\" commonly.");
		Label2.setFont(new Font("Dialog", Font.PLAIN, 14));
		GridBagConstraints gbc_Label2 = new GridBagConstraints();
		gbc_Label2.anchor = GridBagConstraints.NORTHWEST;
		gbc_Label2.insets = new Insets(0, 0, 5, 0);
		gbc_Label2.gridx = 0;
		gbc_Label2.gridy = 2;
		panel_2.add(Label2, gbc_Label2);
		
		JLabel lbllike = new JLabel("if you like this extender, Please give me a star on github. thanks! any issue or suggestion also appreciated!");
		lbllike.setFont(new Font("Dialog", Font.PLAIN, 14));
		GridBagConstraints gbc_lbllike = new GridBagConstraints();
		gbc_lbllike.anchor = GridBagConstraints.NORTHWEST;
		gbc_lbllike.gridx = 0;
		gbc_lbllike.gridy = 6;
		panel_2.add(lbllike, gbc_lbllike);
	}

}
