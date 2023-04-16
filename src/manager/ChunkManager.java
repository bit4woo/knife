package manager;

import java.io.UnsupportedEncodingException;

import burp.BurpExtender;
import burp.HelperPlus;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.ConfigEntry;
import config.GUI;

public class ChunkManager {

	public static void doChunk(boolean messageIsRequest,IHttpRequestResponse message){

		ConfigEntry rule = GUI.tableModel.getConfigByKey("Chunked-AutoEnable");

		if (rule != null) {

			HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
			getter.addOrUpdateHeader(messageIsRequest, message,"Transfer-Encoding", " chunked");
			byte[] oldBody = getter.getBody(messageIsRequest, message);
			try {
				boolean useComment = false;
				if (GUI.tableModel.getConfigValueByKey("Chunked-UseComment") != null) {
					useComment = true;
				}
				String lenStr = GUI.tableModel.getConfigValueByKey("Chunked-Length");
				int len = 10;
				if (lenStr != null) {
					len = Integer.parseInt(lenStr);
				}
				byte[] body = Methods.encoding(oldBody, len, useComment);
				getter.UpdateBody(messageIsRequest, message, body);
			} catch (UnsupportedEncodingException e) {
				BurpExtender.getStderr().print(e.getStackTrace());
			}
		}
	}
}