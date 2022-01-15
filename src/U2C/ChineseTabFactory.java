package U2C;

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
public class ChineseTabFactory implements IMessageEditorTabFactory
{
	private static IExtensionHelpers helpers;
	private static IBurpExtenderCallbacks callbacks;


	public ChineseTabFactory(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
	{
		ChineseTabFactory.callbacks = callbacks;
		ChineseTabFactory.helpers = helpers;
	}

	@Override
	public IMessageEditorTab createNewInstance(IMessageEditorController controller, boolean editable) {
		return new ChineseTab(controller,editable,helpers,callbacks);
	}
}