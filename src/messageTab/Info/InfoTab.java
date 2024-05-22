package messageTab.Info;

import java.awt.Component;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import com.bit4woo.utilbox.utils.ByteArrayUtils;
import com.bit4woo.utilbox.utils.EmailUtils;
import com.bit4woo.utilbox.utils.TextUtils;
import com.bit4woo.utilbox.utils.UrlUtils;

import base.FindUrl_Action;
import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import knife.FindUrlAndRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * @author bit4woo
 * @version CreateTime：2022年1月15日 下午11:07:59
 * <p>
 * 想要正确显示中文内容，有三个编码设置会影响结果：
 * 1、原始编码，通过代码尝试自动获取，但是结果可能不准确，极端情况下需要手动设置。
 * 2、转换后的编码，手动设置。
 * 3、burp设置的显示编码，显示时时用的编码，应该和转换后的编码一致。
 * <p>
 * 原始数据是byte[],但也是文本内容的某种编码的byte[].
 * @github https://github.com/bit4woo
 */
public class InfoTab implements IMessageEditorTab {
    private JPanel panel;

    private byte[] originContent;

    public byte[] getOriginContent() {
		return originContent;
	}

	public void setOriginContent(byte[] originContent) {
		this.originContent = originContent;
	}

	public InfoTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks) {
        panel = new InfoPanel(this);
        BurpExtender.getCallbacks().customizeUiComponent(panel);//尝试使用burp的font size
    }

    @Override
    public String getTabCaption() {
        return "Info";
    }

    @Override
    public Component getUiComponent() {
        return panel;
    }

    @Override
    public boolean isEnabled(byte[] content, boolean isRequest) {
        String contentType = BurpExtender.getHelperPlus().getHeaderValueOf(isRequest, content, "Content-Type");
        if (StringUtils.isEmpty(contentType)) {
            return true;
        }
        if (contentType.contains("image/")) {
            return false;
        } else if (contentType.contains("text/css")) {
            return false;
        } else if (contentType.contains("font/")) {
            return false;
        } else if (contentType.contains("x-protobuf")) {
            return false;
        }

        return true;
    }

    /**
     * 每次切换到这个tab，都会调用这个函数。应考虑避免重复劳动，根据originContent是否变化来判断。
     */
    @Override
    public void setMessage(byte[] content, boolean isRequest) {
        if (content == null || content.length == 0) {
            return;
        } else if (ByteArrayUtils.equals(originContent, content)) {
            return;
        } else {
            originContent = content;
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                	
                	List<String> urls = FindUrl_Action.findUrls(content);

                    for (String url : urls) {
                        InfoEntry aaa = new InfoEntry(url, InfoEntry.Type_URL);
                        ((InfoPanel) panel).getTable().getInfoTableModel().addNewInfoEntry(aaa);
                    }

                    List<String> emails = EmailUtils.grepEmail(new String(content));
                    emails = TextUtils.deduplicate(emails);
                    for (String email : emails) {
                        InfoEntry aaa = new InfoEntry(email, InfoEntry.Type_Email);
                        ((InfoPanel) panel).getTable().getInfoTableModel().addNewInfoEntry(aaa);
                    }

                    return null;
                }
            };
            worker.execute();
        }
    }


    /**
     * 中文下的编辑还是有问题，暂不支持。
     * 始终返回原始内容。
     */
    @Override
    public byte[] getMessage() {
        return originContent;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * ctrl+c复制数据逻辑会调用这个函数
     */
    @Override
    public byte[] getSelectedData() {
        InfoTable table = (InfoTable) ((InfoPanel) panel).getTable();
        String content = table.getSelectedContent();
        return content.getBytes();
    }


    public static void main(String[] args) {
    }
}