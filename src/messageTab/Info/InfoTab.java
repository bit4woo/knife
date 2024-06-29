package messageTab.Info;

import java.awt.Component;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.utils.ByteArrayUtils;
import com.bit4woo.utilbox.utils.EmailUtils;
import com.bit4woo.utilbox.utils.TextUtils;

import base.FindUrlAction;
import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;

/**
 * @author bit4woo
 * @github https://github.com/bit4woo
 */
public class InfoTab implements IMessageEditorTab {
	private JPanel panel;
	private byte[] originContent;
	public IMessageEditorController controller;

	int triggerTime = 1;
	boolean debug = false;

	public byte[] getOriginContent() {
		return originContent;
	}

	public void setOriginContent(byte[] originContent) {
		this.originContent = originContent;
	}


	public IMessageEditorController getController() {
		return controller;
	}

	public void setController(IMessageEditorController controller) {
		this.controller = controller;
	}

	public InfoTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks) {
		this.controller = controller;
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
		if (isRequest) {
			return false;
		}
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
	 * 测试发现:
	 * 当在proxy页面点击一个数据包进行数据包切换时，触发infoTab处理逻辑，同一个数据包这个函数会被触发2次！
	 * <p>
	 * 不是请求、响应各调用一次；而是响应包被调用两次，请求包被调用两次。而且this是同一个对象。
	 * <p>
	 * 第一次触发，content内容不是当前数据包（点击选择的数据包）的，而是上一个数据包的。
	 * 第二次触发，content才是当前数据包的内容，也就是点击选择的数据包的内容。
	 * 造成一个结果就是，切换到新的数据包后，上一个数据包中提取到的内容会在当前数据包中显示。
	 */
	@Override
	public void setMessage(byte[] content, boolean isRequest) {
		if (isRequest) {
			return;
		}

		//boolean debug = true;
		if (debug) {
			System.out.println("\n\n##################");
			System.out.println("triggerTime:" + triggerTime++);
			System.out.println(controller.getHttpService());
			System.out.println("content from controller:\n" + new String(controller.getResponse()));
			System.out.println("content from parameter:\n" + new String(content));//切换数据包时，第一次的触发会发现这个内容是上一个数据包的。
			System.out.println("equal:\n" + ByteArrayUtils.equals(controller.getResponse(), content));
			System.out.println(this);
			System.out.println(((InfoPanel) panel).getTable().getInfoTableModel());
			System.out.println("##################");
		}

		content = controller.getResponse();
		//从controller中获取真实的数据包，避免上面提到的，content是上一个数据包的问题。
		if (content == null || content.length == 0) {
			return;
		} else if (ByteArrayUtils.equals(originContent, content)) {
			return;
		} else {
			originContent = content;
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					((InfoPanel) panel).getTable().getInfoTableModel().clear();
					List<String> urls = FindUrlAction.findUrls(originContent);

					for (String url : urls) {
						InfoEntry aaa = new InfoEntry(url, InfoEntry.Type_URL);
						((InfoPanel) panel).getTable().getInfoTableModel().addNewInfoEntry(aaa);
					}

					List<String> emails = EmailUtils.grepEmail(new String(originContent));
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