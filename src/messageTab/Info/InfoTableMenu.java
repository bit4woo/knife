package messageTab.Info;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.utils.SystemUtils;

import base.FindUrlAction;


public class InfoTableMenu extends JPopupMenu {


	private static final long serialVersionUID = 1L;
	/**
	 * 这处理传入的行index数据是经过转换的 model中的index，不是原始的JTable中的index。
	 *
	 * @param infoTable
	 */
	InfoTableMenu(final InfoTable infoTable) {

		JMenuItem numItem = new JMenuItem(infoTable.getSelectedRows().length + " items selected");

		JMenuItem copyItem = new JMenuItem(new AbstractAction("Copy (double click)") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String content = infoTable.getSelectedContent();
				SystemUtils.writeToClipboard(content);
			}
		});

		JMenuItem changeBaseUrlItem = new JMenuItem(new AbstractAction("Set/Change Base URL") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String targetBaseUrl = infoTable.getTargetBaseUrl();
				List<String> allUrlsOfTarget = infoTable.getAllUrlsOfTarget();
				String baseurl = infoTable.choseBaseUrlToRequest(allUrlsOfTarget);

				if (StringUtils.isNotEmpty(targetBaseUrl) && StringUtils.isNotEmpty(baseurl)) {
					FindUrlAction.httpServiceBaseUrlMap.put(targetBaseUrl, baseurl);
				}
			}
		});

		JMenuItem doRequestItem = new JMenuItem(new AbstractAction("Request URL With Burp Proxy") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				List<String> urls = infoTable.getSelectedUrls();
				infoTable.doRequestUrl(urls);
			}
		});


		JMenuItem openInBrowerItem = new JMenuItem(new AbstractAction("Open URL In Brower") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				List<String> urls = infoTable.getSelectedUrls();
				infoTable.doOpenUrlInBrowser(urls);
			}
		});

		add(numItem);
		add(copyItem);
		add(doRequestItem);
		add(openInBrowerItem);
		add(changeBaseUrlItem);
	}
}
