package config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileFilter;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;

public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String ExtensionName = "Knife v1.7 by bit4woo";
	public String github = "https://github.com/bit4woo/knife";

	public Config config = new Config("default");

	public PrintWriter stdout;
	public PrintWriter stderr;
	//public ConfigTableModel tableModel; 

	private JPanel contentPane;
	private JPanel FooterPanel;
	private JLabel lblNewLabel_2;
	protected JScrollPane configPanel;
	private SortOrder sortedMethod;
	public ConfigTable table;//create in burpextender.java
	public ConfigTableModel tableModel;//create in burpextender.java
	private JButton RemoveButton;
	private JButton AddButton;
	private JSplitPane TargetSplitPane;
	public JLabel lblNewLabel_1;
	public JCheckBox chckbx_proxy;
	public JCheckBox chckbx_repeater;
	public JCheckBox chckbx_intruder;
	private JCheckBox chckbx_scanner;
	private JCheckBox chckbx_scope;

	private JButton RestoreButton;
	private JPanel panel_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.showToUI(new Config(""));
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
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1174, 497);
		contentPane =  new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);


		stdout = new PrintWriter(System.out, true);
		stderr = new PrintWriter(System.out, true);

		///////////////////////HeaderPanel//////////////

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		FlowLayout fl_panel = (FlowLayout) panel.getLayout();
		fl_panel.setAlignment(FlowLayout.LEFT);
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));

		JLabel lblNewLabel = new JLabel("Requests that in : [");
		panel.add(lblNewLabel);

		chckbx_proxy = new JCheckBox("Proxy");
		chckbx_proxy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config.setEnableStatus(checkEnabledFor());
			}
		});
		chckbx_proxy.setSelected(true);
		panel.add(chckbx_proxy);

		chckbx_repeater = new JCheckBox("Repeater");
		chckbx_repeater.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config.setEnableStatus(checkEnabledFor());
			}
		});
		panel.add(chckbx_repeater);

		chckbx_intruder = new JCheckBox("Intruder");
		chckbx_intruder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config.setEnableStatus(checkEnabledFor());
			}
		});
		panel.add(chckbx_intruder);

		chckbx_scanner = new JCheckBox("Scanner ]");
		chckbx_scanner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config.setEnableStatus(checkEnabledFor());
			}
		});
		panel.add(chckbx_scanner);

		JLabel lblNewLabel_display = new JLabel(" AND [");
		panel.add(lblNewLabel_display);

		chckbx_scope = new JCheckBox("also In Scope ]");
		chckbx_scope.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config.setOnlyForScope(chckbx_scope.isSelected());
			}
		});
		chckbx_scope.setSelected(false);
		panel.add(chckbx_scope);

		JLabel lblNewLabel_display1 = new JLabel(" will be auto updated");
		panel.add(lblNewLabel_display1);

		////////////////////////////////////config area///////////////////////////////////////////////////////


		configPanel = new JScrollPane();
		configPanel.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		//contentPane.add(TargetPanel, BorderLayout.WEST);
		
		//table and tableModel created in burpextender.java
		//table = new ConfigTable(new ConfigTableModel());
		

		TargetSplitPane = new JSplitPane();
		TargetSplitPane.setResizeWeight(0.5);
		TargetSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		contentPane.add(TargetSplitPane, BorderLayout.CENTER);

		TargetSplitPane.setLeftComponent(configPanel);


		///////////////////////////////Target Operations and Config//////////////////////


		panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		TargetSplitPane.setRightComponent(panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		
		AddButton = new JButton("Add New Line");
		AddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//tableModel = table.getModel();
				tableModel.addNewConfigEntry(new ConfigEntry("","","",true));
				stdout.println("add: "+new Gson().toJson(config));
				saveConfigToBurp();
				//会触发modelListener 更新config。所以需要调用showToUI。
				//showToUI(config);
			}
		});
		panel_1.add(AddButton);


		RemoveButton = new JButton("Remove Selected Line");
		panel_1.add(RemoveButton);
		RemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] rowindexs = table.getSelectedModelRows();
				tableModel.removeRows(rowindexs);
				saveConfigToBurp();
			}
		});
		
		
		JButton btnSave = new JButton("Save Config");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfigToBurp();
			}});
		btnSave.setToolTipText("Save Config To A File");
		panel_1.add(btnSave);
		
		panel_1.add(new Label(" |"));
		
		JButton btnOpen = new JButton("Import Config(Override)");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				JsonFileFilter jsonFilter = new JsonFileFilter(); //excel过滤器  
				fc.addChoosableFileFilter(jsonFilter);
				fc.setFileFilter(jsonFilter);
				fc.setDialogTitle("Chose knife config File");
				fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
				if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					try {
						File file=fc.getSelectedFile();
						String contents = Files.toString(file, Charsets.UTF_8);
						config = new Gson().fromJson(contents, Config.class);
						stdout.println("Load knife config from"+ file.getName());
						//List<String> lines = Files.readLines(file, Charsets.UTF_8);
						showToUI(config);

					} catch (IOException e1) {
						e1.printStackTrace(stderr);
					}
				}
			}
		});
		btnOpen.setToolTipText("Load Config File");
		panel_1.add(btnOpen);
		
		JButton btnImport = new JButton("Import Config(Combine)");
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fc=new JFileChooser();
				JsonFileFilter jsonFilter = new JsonFileFilter(); //过滤器  
				fc.addChoosableFileFilter(jsonFilter);
				fc.setFileFilter(jsonFilter);
				fc.setDialogTitle("Chose knife config File");
				fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
				if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					try {
						File file=fc.getSelectedFile();
						String contents = Files.toString(file, Charsets.UTF_8);
						config = new Gson().fromJson(contents, Config.class);
						List<String> newEntries = config.getStringConfigEntries();
						List<String> newEntryNames = new ArrayList<String>(); 
						for (String config:newEntries) {
							ConfigEntry entry  = new ConfigEntry().FromJson(config);
							newEntryNames.add(entry.getKey());
						}
						
						List<ConfigEntry> currentEntries = tableModel.getConfigEntries();
						
						for (ConfigEntry config:currentEntries) {
							if (!newEntryNames.contains(config.getKey())){
								newEntries.add(config.ToJson());
							}
						}
						//config.setStringConfigEntries(newEntries);
						stdout.println("Combined knife config from"+ file.getName() +"with current config" );
						//List<String> lines = Files.readLines(file, Charsets.UTF_8);
						showToUI(config);

					} catch (IOException e1) {
						e1.printStackTrace(stderr);
					}
				}
				
				saveConfigToBurp();
			}});
		btnImport.setToolTipText("Combine your configration with current");
		panel_1.add(btnImport);

		JButton btnExport = new JButton("Export Config");
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfigToBurp();
				saveDialog();
			}});
		btnExport.setToolTipText("Export Config To A File");
		panel_1.add(btnExport);

		RestoreButton = new JButton("Restore Defaults");
		RestoreButton.setToolTipText("Restore all config to default!");
		RestoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//TODO need confirm
				showToUI(new Config().FromJson(initConfig()));
			}
		});
		panel_1.add(RestoreButton);
		
		JButton testButton = new JButton("test");
		testButton.setToolTipText("test");
		testButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		//panel_1.add(testButton);
		
		///////////////////////////FooterPanel//////////////////


		FooterPanel = new JPanel();
		FlowLayout fl_FooterPanel = (FlowLayout) FooterPanel.getLayout();
		fl_FooterPanel.setAlignment(FlowLayout.LEFT);
		contentPane.add(FooterPanel, BorderLayout.SOUTH);

		lblNewLabel_2 = new JLabel(ExtensionName+"    "+github);
		lblNewLabel_2.setFont(new Font("宋体", Font.BOLD, 12));
		lblNewLabel_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					URI uri = new URI(github);
					Desktop desktop = Desktop.getDesktop();
					if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
						desktop.browse(uri);
					}
				} catch (Exception e2) {
					e2.printStackTrace(stderr);
				}

			}
			@Override
			public void mouseEntered(MouseEvent e) {
				lblNewLabel_2.setForeground(Color.BLUE);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				lblNewLabel_2.setForeground(Color.BLACK);
			}
		});
		FooterPanel.add(lblNewLabel_2);

	}


	//////////////////////////////methods//////////////////////////////////////

	public void showToUI(Config config) {
		tableModel = table.getModel();
		tableModel.setConfigEntries(new ArrayList<ConfigEntry>());
		//clearTable
		
		for (String stringEntry:config.getStringConfigEntries()) {
			ConfigEntry entry  = new ConfigEntry().FromJson(stringEntry);
			tableModel.addNewConfigEntry(entry);
		}
		table.setupTypeColumn();// must setup again when data cleaned
		

		if (IBurpExtenderCallbacks.TOOL_INTRUDER ==(config.getEnableStatus() & IBurpExtenderCallbacks.TOOL_INTRUDER)) {
			chckbx_intruder.setSelected(true);
		}else {
			chckbx_intruder.setSelected(false);
		}
		if (IBurpExtenderCallbacks.TOOL_PROXY ==(config.getEnableStatus() & IBurpExtenderCallbacks.TOOL_PROXY)) {
			chckbx_proxy.setSelected(true);
		}else {
			chckbx_proxy.setSelected(false);
		}
		if (IBurpExtenderCallbacks.TOOL_REPEATER ==(config.getEnableStatus() & IBurpExtenderCallbacks.TOOL_REPEATER)) {
			chckbx_repeater.setSelected(true);
		}else {
			chckbx_repeater.setSelected(false);
		}
		if (IBurpExtenderCallbacks.TOOL_SCANNER ==(config.getEnableStatus() & IBurpExtenderCallbacks.TOOL_SCANNER)) {
			chckbx_scanner.setSelected(true);
		}else {
			chckbx_scanner.setSelected(false);
		}
		chckbx_scope.setSelected(config.isOnlyForScope());
	}

	public String getAllConfig() {
		config.setStringConfigEntries(tableModel.getConfigJsons());
		return config.ToJson();
	}
	
	public void saveConfigToBurp() {
		BurpExtender.callbacks.saveExtensionSetting("knifeconfig", getAllConfig());
	}

	public int checkEnabledFor(){
		//get values that should enable this extender for which Component.
		int status = 0;
		if (chckbx_intruder.isSelected()){
			status += IBurpExtenderCallbacks.TOOL_INTRUDER;
		}
		if(chckbx_proxy.isSelected()){
			status += IBurpExtenderCallbacks.TOOL_PROXY;
		}
		if(chckbx_repeater.isSelected()){
			status += IBurpExtenderCallbacks.TOOL_REPEATER;
		}
		if(chckbx_scanner.isSelected()) {
			status += IBurpExtenderCallbacks.TOOL_SCANNER;
		}
		return status;
	}


	public void saveDialog() {
		JFileChooser fc=new JFileChooser();
		JsonFileFilter jsonFilter = new JsonFileFilter(); //excel过滤器  
		fc.addChoosableFileFilter(jsonFilter);
		fc.setFileFilter(jsonFilter);
		fc.setDialogTitle("Save Config To A File:");
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		if(fc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION){
			File file=fc.getSelectedFile();

			if(!(file.getName().toLowerCase().endsWith(".json"))){
				file=new File(fc.getCurrentDirectory(),file.getName()+".json");
			}

			String content= getAllConfig();
			try{
				if(file.exists()){
					int result = JOptionPane.showConfirmDialog(null,"Are you sure to overwrite this file ?");
					if (result == JOptionPane.YES_OPTION) {
						file.createNewFile();
					}else {
						return;
					}
				}else {
					file.createNewFile();
				}

				Files.write(content.getBytes(), file);
			}catch(Exception e1){
				e1.printStackTrace(stderr);
			}
		}
	}
	
	public String initConfig() {
		// need to override
		return null;
	}


	class JsonFileFilter extends FileFilter {
		public String getDescription() {  
			return "*.json";  
		}  

		public boolean accept(File file) {
			String name = file.getName();
			return file.isDirectory() || name.toLowerCase().endsWith(".json");  // 仅显示目录和json文件
		}
	}
}
