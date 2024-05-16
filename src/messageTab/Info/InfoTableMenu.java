package messageTab.Info;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.bit4woo.utilbox.utils.SystemUtils;

import burp.BurpExtender;


public class InfoTableMenu extends JPopupMenu {


	private static final long serialVersionUID = 1L;
	PrintWriter stdout = BurpExtender.getStdout();
	PrintWriter stderr = BurpExtender.getStderr();

	/**
	 * 这处理传入的行index数据是经过转换的 model中的index，不是原始的JTable中的index。
	 * @param modelRows
	 * @param columnIndex
	 */
	InfoTableMenu(final InfoTable infoTable){

		JMenuItem copyItem = new JMenuItem(new AbstractAction("Copy") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String content = infoTable.getSelectedContent();
				SystemUtils.writeToClipboard(content);
			}
		});

		add(copyItem);
	}
}
