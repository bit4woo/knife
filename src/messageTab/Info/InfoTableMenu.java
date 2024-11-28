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

		JMenuItem copyItem = new JMenuItem(new AbstractAction("Copy (Ctrl+C)") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String content = infoTable.getSelectedContent();
				SystemUtils.writeToClipboard(content);
			}
		});

		JMenuItem changeBaseUrlItem = new JMenuItem(new AbstractAction("Set Base URL") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				String originUrl = infoTable.getOriginUrl();
				List<String> allUrlsOfTarget = infoTable.getAllUrlsOfTarget();
				
				String baseurl = infoTable.choseBaseUrlToRequest(allUrlsOfTarget);

				if (StringUtils.isNotEmpty(originUrl) && StringUtils.isNotEmpty(baseurl)) {
					FindUrlAction.httpServiceBaseUrlMap.put(originUrl, baseurl);
					((InfoPanelHeadPanel)(InfoPanel.getHeadPanel())).setBaseUrl(baseurl);
				}
			}
		});
		
		JMenuItem setSelectedAsBaseUrlItem = new JMenuItem(new AbstractAction("Set Selected Item As Base URL") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				
				String originUrl = infoTable.getOriginUrl();
				//List<String> allUrlsOfTarget = infoTable.getAllUrlsOfTarget();
				List<String> urls = infoTable.getSelectedUrls();
				String baseurl = infoTable.choseBaseUrlToRequest(urls);

				if (StringUtils.isNotEmpty(originUrl) && StringUtils.isNotEmpty(baseurl)) {
					FindUrlAction.httpServiceBaseUrlMap.put(originUrl, baseurl);
					((InfoPanelHeadPanel)(InfoPanel.getHeadPanel())).setBaseUrl(baseurl);
				}
				
			}
		});

		/**
		 * TODO 自动查找对应cookie并用于请求
		 */
		JMenuItem doRequestItem = new JMenuItem(new AbstractAction("Request URL With Burp Proxy") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				List<String> urls = infoTable.getSelectedUrls();
				infoTable.doRequestUrl(urls);
			}
		});


		JMenuItem openInBrowerItem = new JMenuItem(new AbstractAction("Open URL In Brower(Double Click)") {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				List<String> urls = infoTable.getSelectedUrls();
				infoTable.doOpenUrlInBrowser(urls);
			}
		});

		add(numItem);
		add(copyItem);
		
		this.addSeparator();
		add(changeBaseUrlItem);
		add(setSelectedAsBaseUrlItem);
		
		this.addSeparator();
		add(openInBrowerItem);
		add(doRequestItem);
		
	}
}
