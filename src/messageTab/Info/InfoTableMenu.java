package messageTab.Info;

import base.FindUrlAction;
import com.bit4woo.utilbox.utils.SystemUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;


public class InfoTableMenu extends JPopupMenu {


    private static final long serialVersionUID = 1L;
    /**
     * 这处理传入的行index数据是经过转换的 model中的index，不是原始的JTable中的index。
     *
     * @param infoTable
     */
    InfoTableMenu(final InfoTable infoTable) {

        JMenuItem numItem = new JMenuItem(infoTable.getSelectedRows().length + " items selected");

        JMenuItem copyItem = new JMenuItem(new AbstractAction("Copy") {
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

        JMenuItem doRequestItem = new JMenuItem(new AbstractAction("Do Request") {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                List<String> urls = infoTable.getSelectedUrls();
                infoTable.doRequestUrl(urls);
            }
        });

        add(numItem);
        add(copyItem);
        add(doRequestItem);
        add(changeBaseUrlItem);
    }
}
