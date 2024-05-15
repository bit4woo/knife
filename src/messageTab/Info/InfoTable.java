package messageTab.Info;

import static config.ConfigTableModel.titles;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;

import com.bit4woo.utilbox.utils.SystemUtils;

import burp.BurpExtender;


public class InfoTable extends JTable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String[] headers = {"Value", "Type"};


    public InfoTable(InfoTableModel tableModel) {
        super(tableModel);
        this.setColumnModel(columnModel);
        this.setFillsViewportHeight(true);//在table的空白区域显示右键菜单
        //https://stackoverflow.com/questions/8903040/right-click-mouselistener-on-whole-jtable-component
        this.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        this.setBorder(new LineBorder(new Color(0, 0, 0)));

        addClickSort();
        registerListeners();
        //switchEnable();//no need
        //table.setupTypeColumn()//can't set here, only can after table data loaded.
        //tableHeaderLengthInit();//can't set here, only can after table data loaded.
    }

    public static List<String> getHeaders() {
        return Arrays.asList(headers);
    }


    public InfoTableModel getInfoTableModel() {
        return (InfoTableModel) this.getModel();
    }


    public int[] getSelectedModelRows() {
        int[] rows = getSelectedRows();

        for (int i = 0; i < rows.length; i++) {
            rows[i] = convertRowIndexToModel(rows[i]);//转换为Model的索引，否则排序后索引不对应〿
        }
        Arrays.sort(rows);//升序
        return rows;
    }
    
    public InfoEntry getEntryAt(int row) {
        return ((InfoTableModel) this.getModel()).getEntryAt(convertRowIndexToModel(row));
    }

    private void addClickSort() {
        TableRowSorter<InfoTableModel> sorter = new TableRowSorter<>((InfoTableModel) this.getModel());
        this.setRowSorter(sorter);
    }

    /**
     * 需要在数据加载后，即setModel后才有效果!
     */
    public void tableHeaderLengthInit() {
        Font f = this.getFont();
        FontMetrics fm = this.getFontMetrics(f);
        int width = fm.stringWidth("A");//一个字符的宽度
        for (int index = 0; index < this.getColumnCount(); index++) {
            TableColumn column = this.getColumnModel().getColumn(index);

            if (column.getIdentifier().equals("#")) {
                column.setMaxWidth(width * "100".length());
            }

            if (column.getIdentifier().equals("Enable")) {
                column.setMaxWidth(width * "Enable++".length());
                //需要预留排序时箭头符合的位置，2个字符宽度
            }
        }
        //this.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);//配合横向滚动条
    }


    //将选中的行（图形界面的行）转换为Model中的行数（数据队列中的index）.因为图形界面排序等操作会导致图像和数据队列的index不是线性对应的。
    public int[] SelectedRowsToModelRows(int[] SelectedRows) {

        for (int i = 0; i < SelectedRows.length; i++) {
            SelectedRows[i] = convertRowIndexToModel(SelectedRows[i]);//转换为Model的索引，否则排序后索引不对应〿
        }
        Arrays.sort(SelectedRows);//升序

        return SelectedRows;
    }

    private void registerListeners() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    InfoTable target = (InfoTable) e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();

                    //双击浏览器打开url
                    if (headers[column].equalsIgnoreCase("Value")) {//双击url在浏览器中打开
                        try {
                        	InfoEntry entry = getEntryAt(row);
                        	if (entry.getType().equals(InfoEntry.Type_URL)) {
                                String url = (String) getValueAt(row, column);
                                if (url.toLowerCase().startsWith("http://") || url.toLowerCase().startsWith("https://")) {
                                	String browserPath = BurpExtender.getConfigTableModel().getConfigValueByKey("browserPath");
                                    SystemUtils.browserOpen(url, browserPath);
                                }
                        	}
                        } catch (Exception e1) {
                            e1.printStackTrace(BurpExtender.getStderr());
                        }
                    }
                }
            }

            @Override//title表格中的鼠标右键菜单
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    if (e.isPopupTrigger() && e.getComponent() instanceof InfoTable) {
                        int[] rows = getSelectedRows();
                        int col = ((InfoTable) e.getSource()).columnAtPoint(e.getPoint()); // 获得列位置
                        int modelCol = InfoTable.this.convertColumnIndexToModel(col);
                        if (rows.length > 0) {
                            int[] modelRows = SelectedRowsToModelRows(rows);
                            //new ConfigTableMenu(ConfigTable.this, modelRows, modelCol).show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                //no need
            }
        });
    }


    /**
     * 搜索功能
     * @param caseSensitive
     */
    /*
	public void search(String Input,boolean caseSensitive) {
		final RowFilter filter = new RowFilter() {
			@Override
			public boolean include(Entry entry) {
				int row = (int) entry.getIdentifier();
				ConfigEntry line = getInfoTableModel().getConfigEntries().get(row);
				if (caseSensitive) {
					return line.ToJson().contains(Input);
				}else {
					return line.ToJson().toLowerCase().contains(Input.toLowerCase());
				}
			}
		};
		((TableRowSorter)getRowSorter()).setRowFilter(filter);
	}
	*/
}
