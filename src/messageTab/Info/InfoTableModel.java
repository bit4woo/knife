package messageTab.Info;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import burp.BurpExtender;


public class InfoTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<InfoEntry> infoEntries = new ArrayList<>();

	public static String[] titles = InfoTable.headers;

	public InfoTableModel() {

	}

	////////////////////// extend AbstractTableModel////////////////////////////////

	@Override
	public int getColumnCount() {
		return titles.length;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (titles[columnIndex].equals("#")) {
			return Integer.class;//index
		} else if (titles[columnIndex].equals("Enable")) {
			return boolean.class;//enable
		} else {
			return String.class;
		}
	}

	@Override
	public int getRowCount() {
		return infoEntries.size();
	}

	//define header of table???
	@Override
	public String getColumnName(int columnIndex) {
		if (columnIndex >= 0 && columnIndex <= titles.length) {
			return titles[columnIndex];
		} else {
			return "";
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public InfoEntry getEntryAt(int rowIndex) {
		return infoEntries.get(rowIndex);
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		InfoEntry entry = infoEntries.get(rowIndex);
		if (titles[columnIndex].equals("#")) {
			return rowIndex;
		} else if (titles[columnIndex].equals("Value")) {
			return entry.getValue();
		} else if (titles[columnIndex].equals("Type")) {
			return entry.getType();
		} else if (titles[columnIndex].equals("Enable")) {
			return entry.isEnable();
		} else if (titles[columnIndex].equals("Comment")) {
			return entry.getComment();
		} else {
			return "";
		}
	}


	/*
	 * Don't need to implement this method unless your table's
	 * data can change.
	 */
	@Override
	public void setValueAt(Object value, int row, int columnIndex) {
		InfoEntry entry = infoEntries.get(row);

		if (titles[columnIndex].equals("#")) {

		} else if (titles[columnIndex].equals("Value")) {
			entry.setValue((String) value);
		} else if (titles[columnIndex].equals("Type")) {
			entry.setType((String) value);
		} else if (titles[columnIndex].equals("Enable")) {
			entry.setEnable((boolean) value);
		} else if (titles[columnIndex].equals("Comment")) {
			entry.setComment((String) value);
		}
		fireTableCellUpdated(row, columnIndex);
	}

	//////////////////////extend AbstractTableModel////////////////////////////////

	public void addNewInfoEntry(InfoEntry lineEntry) {
		synchronized (infoEntries) {
			infoEntries.add(lineEntry);
			int row = infoEntries.size();
			//fireTableRowsInserted(row, row);
			//need to use row-1 when add setRowSorter to table. why??
			//https://stackoverflow.com/questions/6165060/after-adding-a-tablerowsorter-adding-values-to-model-cause-java-lang-indexoutofb
			fireTableRowsInserted(row - 1, row - 1);
			//fireTableRowsInserted(row-2, row-2);
		}
	}


	public void removeInfoEntry(InfoEntry lineEntry) {
		synchronized (infoEntries) {
			int index = infoEntries.indexOf(lineEntry);
			if (index != -1) {
				infoEntries.remove(lineEntry);
				fireTableRowsDeleted(index, index);
			}
		}
	}

	public void removeRows(int[] rows) {
		PrintWriter stdout1 = new PrintWriter(BurpExtender.callbacks.getStdout(), true);
		synchronized (infoEntries) {
			//because thread let the delete action not in order, so we must loop in here.
			//list length and index changed after every remove.the origin index not point to right item any more.
			Arrays.sort(rows); //升序
			for (int i = rows.length - 1; i >= 0; i--) {//降序删除才能正确删除每个元素
				InfoEntry config = infoEntries.get(rows[i]);
				infoEntries.remove(rows[i]);
				stdout1.println("!!! " + config.getValue() + " deleted");
				this.fireTableRowsDeleted(rows[i], rows[i]);
			}
		}
	}

	public void clear() {
		synchronized (infoEntries) {
			infoEntries = new ArrayList<>();
		}
	}

	public void updateRows(int[] rows) {
		synchronized (infoEntries) {
			//because thread let the delete action not in order, so we must loop in here.
			//list length and index changed after every remove.the origin index not point to right item any more.
			Arrays.sort(rows); //升序
			for (int i = rows.length - 1; i >= 0; i--) {//降序删除才能正确删除每个元素
				InfoEntry checked = infoEntries.get(rows[i]);
				infoEntries.remove(rows[i]);
				infoEntries.add(rows[i], checked);
			}
			this.fireTableRowsUpdated(rows[0], rows[rows.length - 1]);
		}
	}

	public List<InfoEntry> getConfigEntries() {
		return infoEntries;
	}


	public void setConfigEntries(List<InfoEntry> configEntries) {
		this.infoEntries = configEntries;
	}

}