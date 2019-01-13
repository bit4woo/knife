package config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import burp.IBurpExtenderCallbacks;
import config.ConfigObject;

public class GUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String ExtensionName = "Knife v0.9 by bit4woo";
	public String github = "https://github.com/bit4woo/knife";

	public ConfigObject config = new ConfigObject("default");

	public PrintWriter stdout;
	public PrintWriter stderr;
	private DefaultTableModel tableModel; 

	private JPanel contentPane;
	private JPanel FooterPanel;
	private JLabel lblNewLabel_2;
	private JScrollPane configPanel;
	private SortOrder sortedMethod;
	private JTable table;
	private JButton RemoveButton;
	private JButton AddButton;
	private JSplitPane TargetSplitPane;
	public JLabel lblNewLabel_1;
	public JCheckBox chckbx_proxy;
	public JCheckBox chckbx_repeater;
	public JCheckBox chckbx_intruder;
	private JCheckBox chckbx_scanner;
	private JCheckBox chckbx_scope;

	private boolean DoNotTrigger = true;
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
					frame.showToUI(new ConfigObject(""));
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
				//config.enableStatus = checkEnabledFor();
				//saveConfig have done this
				saveConfig();
				stdout.println("Proxy: "+JSON.toJSONString(config));
			}
		});
		chckbx_proxy.setSelected(true);
		panel.add(chckbx_proxy);

		chckbx_repeater = new JCheckBox("Repeater");
		chckbx_repeater.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//config.enableStatus = checkEnabledFor();
				//saveConfig have done this
				saveConfig();
			}
		});
		panel.add(chckbx_repeater);

		chckbx_intruder = new JCheckBox("Intruder");
		chckbx_intruder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//config.enableStatus = checkEnabledFor();
				//saveConfig have done this
				saveConfig();
			}
		});
		panel.add(chckbx_intruder);

		chckbx_scanner = new JCheckBox("Scanner ]");
		chckbx_scanner.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//config.enableStatus = checkEnabledFor();
				//saveConfig have done this
				saveConfig();
			}
		});
		panel.add(chckbx_scanner);

		JLabel lblNewLabel_display = new JLabel(" AND [");
		panel.add(lblNewLabel_display);

		chckbx_scope = new JCheckBox("also In Scope ]");
		chckbx_scope.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//config.onlyForScope = chckbx_scope.isSelected();
				//saveConfig have done this
				saveConfig();
			}
		});
		chckbx_scope.setSelected(false);
		panel.add(chckbx_scope);

		JLabel lblNewLabel_display1 = new JLabel(" will be auto updated with <append> <add-or-replace> items");
		panel.add(lblNewLabel_display1);

		////////////////////////////////////config area///////////////////////////////////////////////////////


		configPanel = new JScrollPane();
		configPanel.setViewportBorder(new LineBorder(new Color(0, 0, 0)));
		//contentPane.add(TargetPanel, BorderLayout.WEST);

		table = new JTable();
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));

		table.getTableHeader().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					table.getRowSorter().getSortKeys().get(0).getColumn();
					//System.out.println(sortedColumn);
					sortedMethod = table.getRowSorter().getSortKeys().get(0).getSortOrder();
					System.out.println(sortedMethod); //ASCENDING   DESCENDING
				} catch (Exception e1) {
					sortedMethod = null;
					e1.printStackTrace(stderr);
				}
			}
		});

		tableModel = new DefaultTableModel(
				new Object[][] {
					//{"1", "1","1"},
				},
				new String[] {
						"key", "value"//, "Source"
				}
				);
		table.setModel(tableModel);
		tableModel.addTableModelListener(new TableModelListener(){
			@Override
			public void tableChanged(TableModelEvent e) {
				if (DoNotTrigger) {

				}else {
					stdout.println("tableChanged1: "+JSON.toJSONString(config));
					//config.basicConfigs = getTableMap(); //saveConfig have done this
					saveConfig();
					stdout.println("tableChanged2: "+JSON.toJSONString(config));
				}
			}
		});



		RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		table.setRowSorter(sorter);

		table.setColumnSelectionAllowed(true);
		table.setCellSelectionEnabled(true);
		table.setSurrendersFocusOnKeystroke(true);
		table.setFillsViewportHeight(true);
		table.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		configPanel.setViewportView(table);

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

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc=new JFileChooser();
				JsonFileFilter jsonFilter = new JsonFileFilter(); //excel过滤器  
				fc.addChoosableFileFilter(jsonFilter);
				fc.setFileFilter(jsonFilter);
				fc.setDialogTitle("Chose Domain Hunter Project File");
				fc.setDialogType(JFileChooser.CUSTOM_DIALOG);
				if(fc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
					try {
						File file=fc.getSelectedFile();
						String contents = Files.toString(file, Charsets.UTF_8);
						config = JSON.parseObject(contents, ConfigObject.class);
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

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveDialog();
			}});
		btnSave.setToolTipText("Save Config To A File");
		panel_1.add(btnSave);

		AddButton = new JButton("Add");
		AddButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				tableModel.addRow(new Object[]{"",""});
				stdout.println("add: "+JSON.toJSONString(config));
				//会触发modelListener 更新config。所以需要调用showToUI。
				showToUI(config);
			}
		});
		panel_1.add(AddButton);


		RemoveButton = new JButton("Remove");
		panel_1.add(RemoveButton);
		RemoveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int[] rowindexs = table.getSelectedRows();
				for (int i=0; i < rowindexs.length; i++){
					rowindexs[i] = table.convertRowIndexToModel(rowindexs[i]);//转换为Model的索引，否则排序后索引不对应〿
				}
				Arrays.sort(rowindexs);

				tableModel = (DefaultTableModel) table.getModel();
				for(int i=rowindexs.length-1;i>=0;i--){
					tableModel.removeRow(rowindexs[i]);
				}
				// will trigger tableModel listener

				config.basicConfigs = getTableMap();
				showToUI(config);
			}
		});

		RestoreButton = new JButton("Restore");
		RestoreButton.setToolTipText("Restore all config to default!");
		RestoreButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				config = new ConfigObject("default");
				showToUI(config);
			}
		});
		panel_1.add(RestoreButton);



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


	public void showToUI(ConfigObject config) {//this also trigger tableModel listener 
		DoNotTrigger = true;

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		model.setRowCount(0);
		//clearTable

		stdout.println("showToUI:"+JSON.toJSONString(config));

		for (Entry<String, String> entry:config.basicConfigs.entrySet()) {
			tableModel.addRow(new Object[]{entry.getKey(),entry.getValue()});
		}

		if (IBurpExtenderCallbacks.TOOL_INTRUDER ==(config.enableStatus & IBurpExtenderCallbacks.TOOL_INTRUDER)) {
			chckbx_intruder.setSelected(true);
		}else {
			chckbx_intruder.setSelected(false);
		}
		if (IBurpExtenderCallbacks.TOOL_PROXY ==(config.enableStatus & IBurpExtenderCallbacks.TOOL_PROXY)) {
			chckbx_proxy.setSelected(true);
		}else {
			chckbx_proxy.setSelected(false);
		}
		if (IBurpExtenderCallbacks.TOOL_REPEATER ==(config.enableStatus & IBurpExtenderCallbacks.TOOL_REPEATER)) {
			chckbx_repeater.setSelected(true);
		}else {
			chckbx_repeater.setSelected(false);
		}
		if (IBurpExtenderCallbacks.TOOL_SCANNER ==(config.enableStatus & IBurpExtenderCallbacks.TOOL_SCANNER)) {
			chckbx_scanner.setSelected(true);
		}else {
			chckbx_scanner.setSelected(false);
		}
		chckbx_scope.setSelected(config.onlyForScope);
		DoNotTrigger = false;
	}

	public ConfigObject getConfigFromUI() {
		config.basicConfigs = getTableMap();
		config.enableStatus = checkEnabledFor();
		config.onlyForScope = chckbx_scope.isSelected();
		return config;
	}

	public void saveConfig() {
		//burp need to override this function to save extension config
	}

	public LinkedHashMap<String, String> getTableMap() {
		DoNotTrigger = true;
		LinkedHashMap<String,String> tableMap= new LinkedHashMap<String,String>();

		/*		for(int x=0;x<table.getRowCount();x++){
			String key =(String) table.getValueAt(x, 0);
			String value = (String) table.getValueAt(x, 1); //encountered a "ArrayIndexOutOfBoundsException" error here~~ strange!
			tableMap.put(key,value);
		}
		return tableMap;*/

		Vector data = tableModel.getDataVector();
		for (Object o : data) {
			Vector v = (Vector) o;
			String key = (String) v.elementAt(0);
			String value = (String) v.elementAt(1);
			if (key != null && value != null) {
				tableMap.put(key, value);
			}
		}
		DoNotTrigger = false;
		return tableMap;
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

			String content= JSON.toJSONString(config);
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
