package U2C;

import java.awt.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IMessageEditorController;
import burp.IMessageEditorTab;
import burp.ITextEditor;

public class U2CTab implements IMessageEditorTab
{
    private ITextEditor txtInput;
    private byte[] originContent;
    public U2CTab(IMessageEditorController controller, boolean editable, IExtensionHelpers helpers, IBurpExtenderCallbacks callbacks)
    {
        txtInput = callbacks.createTextEditor();
        txtInput.setEditable(editable);
    }

    @Override
    public String getTabCaption()
    {
        return "U2C";
    }

    @Override
    public Component getUiComponent()
    {
        return txtInput.getComponent();
    }

    @Override
    public boolean isEnabled(byte[] content, boolean isRequest)
    {
    	
    	if(content!=null && !isRequest && needtoconvert(new String(content))) {
    		originContent = content;
    		return true;
    	}else {
    		return false;
    	}
    	
    }

    @Override
    public void setMessage(byte[] content, boolean isRequest)
    {
    	String UnicodeResp = "";
    	if(content != null) {
        	String resp= new String(content);
        	try {
            	while (needtoconvert(resp)) {
            		resp = Unicode.unicodeDecode(resp);
            	}
			} catch (Exception e) {
            	while (needtoconvert(resp)) {
            		resp = StringEscapeUtils.unescapeJava(resp);
            	}
			}
        	UnicodeResp = resp;
    	}
    	txtInput.setText(UnicodeResp.getBytes());
    }

    @Override
    public byte[] getMessage()
    {
    	//byte[] text = txtInput.getText();
        //return text;
    	return originContent;
    	//change the return value of getMessage() method to the origin content to tell burp don't change the original response

    }

    @Override
    public boolean isModified()
    {
        //return txtInput.isTextModified();
        return false;
        //change the return value of isModified() method to false. to let burp don't change the original response) 
    }

    @Override
    public byte[] getSelectedData()
    {
        return txtInput.getSelectedText();
    }
    
    
    public static boolean needtoconvert(String str) {
    	Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
    	//Pattern pattern = Pattern.compile("(\\\\u([A-Fa-f0-9]{4}))");//和上面的效果一样
    	Matcher matcher = pattern.matcher(str.toLowerCase());
    	
    	if (matcher.find() ){
    		return true;
//    		String found = matcher.group();
//    		//！@#￥%……&*（）——-=，。；：“‘{}【】+
//    		String chineseCharacter = "\\uff01\\u0040\\u0023\\uffe5\\u0025\\u2026\\u2026\\u0026\\u002a\\uff08\\uff09\\u2014\\u2014\\u002d\\u003d\\uff0c\\u3002\\uff1b\\uff1a\\u201c\\u2018\\u007b\\u007d\\u3010\\u3011\\u002b";
//    		if (("\\u4e00").compareTo(found)<= 0 && found.compareTo("\\u9fa5")<=0)
//    			return true;
//    		else if(chineseCharacter.contains(found)){
//    			return true;
//    		}else{
//    			return false;
//    		}
    	}else {
    		return false;
    	}
    }
    
    public static void main(String args[]) {
		System.out.print(needtoconvert("\\u0000"));
	}
}