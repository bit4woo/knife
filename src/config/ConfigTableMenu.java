package config;

import burp.BurpExtender;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;


public class ConfigTableMenu extends JPopupMenu {


	private static final long serialVersionUID = 1L;
	PrintWriter stdout = BurpExtender.getStdout();
	PrintWriter stderr = BurpExtender.getStderr();
	
	private ConfigTable configTable;

	/**
	 * 这处理传入的行index数据是经过转换的 model中的index，不是原始的JTable中的index。
	 * @param modelRows
	 * @param columnIndex
	 */
	ConfigTableMenu(final GUI gui, final int[] modelRows,final int columnIndex){

		JMenuItem itemNumber = new JMenuItem(new AbstractAction(modelRows.length+" Items Selected") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {

			}
		});
		
		JMenuItem enableItem = new JMenuItem(new AbstractAction("Enable Config") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				for (int row:modelRows) {
					ConfigEntry config = configTable.getConfigTableModel().getConfigEntries().get(row);
					config.setEnable(true);
				}
			}
		});
		
		JMenuItem disableItem = new JMenuItem(new AbstractAction("Disable Config") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				for (int row:modelRows) {
					ConfigEntry config = configTable.getConfigTableModel().getConfigEntries().get(row);
					config.setEnable(false);
				}
			}
		});
		
		add(itemNumber);
		add(enableItem);
		add(disableItem);
	}


}
