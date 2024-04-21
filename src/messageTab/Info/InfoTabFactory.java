package messageTab.Info;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import burp.IMessageEditorTabFactory;

/**
 * 工厂类，构造一个个的Tab实例
 * @author bit4woo
 * @github https://github.com/bit4woo
 *
 */
public class InfoTabFactory implements IMessageEditorTabFactory
{
	private static IExtensionHelpers helpers;
	private static IBurpExtenderCallbacks callbacks;


	public InfoTabFactory(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		InfoTabFactory.callbacks = callbacks;
		InfoTabFactory.helpers = helpers;
	}

	@Override
	public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
		return new InfoTab(controller,editable,helpers,callbacks);
	}
}