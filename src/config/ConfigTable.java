package config;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;
import java.util.Arrays;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import burp.BurpExtender;


public class ConfigTable extends JTable
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ConfigTableModel ConfigTableModel;



	public ConfigTable(ConfigTableModel ConfigTableModel)
	{
		super(ConfigTableModel);
		this.ConfigTableModel = ConfigTableModel;
		this.setColumnModel(columnModel);
		this.setFillsViewportHeight(true);//在table的空白区域显示右键菜单
		//https://stackoverflow.com/questions/8903040/right-click-mouselistener-on-whole-jtable-component
		this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		this.setBorder(new LineBorder(new Color(0, 0, 0)));

		addClickSort();
		registerListeners();
		//setUpTypeColumn(this,this.getColumnModel().getColumn(2));
	}

	@Override
	public ConfigTableModel getModel(){
		return this.ConfigTableModel;
	}

	@Override
	public void changeSelection(int row, int col, boolean toggle, boolean extend)
	{
		super.changeSelection(row, col, toggle, extend);
	}

	public int[] getSelectedModelRows() {
		int[] rows = getSelectedRows();

		for (int i=0; i < rows.length; i++){
			rows[i] = convertRowIndexToModel(rows[i]);//转换为Model的索引，否则排序后索引不对应〿
		}
		Arrays.sort(rows);//升序
		return rows;
	}

	private void addClickSort() {
		TableRowSorter<ConfigTableModel> sorter = new TableRowSorter<ConfigTableModel>(ConfigTableModel);
		ConfigTable.this.setRowSorter(sorter);

		JTableHeader header = this.getTableHeader();
		header.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					//ConfigTable.this.getRowSorter().getSortKeys().get(0).getColumn();
					sorter.getSortKeys().get(0).getColumn();
					////当Jtable中无数据时，jtable.getRowSorter()是null
				} catch (Exception e1) {
					e1.printStackTrace(new PrintWriter(BurpExtender.callbacks.getStderr(), true));//working?
				}
			}
		});
	}

	private void registerListeners(){
		final ConfigTable _this = this;
		this.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseReleased( MouseEvent e ){
			}

			@Override
			public void mousePressed(MouseEvent e) {
				//no need
			}

		});
	}


	public void setUpTypeColumn(ConfigTable table,
			TableColumn typeColumn) {
		//Set up the editor for the sport cells.
		JComboBox comboBox = new JComboBox();
		comboBox.addItem("Snowboarding");
		comboBox.addItem("Rowing");
		comboBox.addItem("Knitting");
		comboBox.addItem("Speed reading");
		comboBox.addItem("Pool");
		comboBox.addItem("None of the above");
		typeColumn.setCellEditor(new DefaultCellEditor(comboBox));

		//Set up tool tips for the sport cells.
		DefaultTableCellRenderer renderer =
				new DefaultTableCellRenderer();
		renderer.setToolTipText("Click for combo box");
		typeColumn.setCellRenderer(renderer);
	}
}
