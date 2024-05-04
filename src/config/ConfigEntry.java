package config;

import static burp.BurpExtender.isInCheckBoxScope;
import static runcmd.MessagePart.getValueByPartType;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.TextUtils;
import com.google.gson.Gson;

import burp.BurpExtender;
import burp.IBurpExtenderCallbacks;
import burp.IHttpRequestResponse;
import burp.IInterceptedProxyMessage;
import runcmd.MessagePart;

public class ConfigEntry {

    private String key = "";
    private String value = "";
    private String type = "";
    private boolean enable = true;
    private boolean editable = true;//whether you can edit name and type
    private String comment = "";

    //所有的数据包操作
    private static final String Action_ = "Action_";

    //大范围的【所有数据包、toolFlag控制范围内的数据包】 数据包修改
    public static final String Action_Add_Or_Replace_Header = "Action_Add_Or_Replace_Header";// scope is controlled by gui
    public static final String Action_Append_To_header_value = "Action_Append_To_header_value";// scope is controlled by gui
    public static final String Action_Remove_From_Headers = "Action_Remove_From_Headers"; //scope is for all request
    public static final String Action_Forward_And_Hide_Options = "Action_Forward_And_Hide_Options"; //scope is for all request


    //范围是URL的数据包修改
    public static final String Action_If_Base_URL_Matches_Add_Or_Replace_Header = "Action_If_Base_URL_Matches_Add_Or_Replace_Header";
    public static final String Action_If_Base_URL_Matches_Append_To_header_value = "Action_If_Base_URL_Matches_Append_To_header_value";
    public static final String Action_If_Base_URL_Matches_Remove_From_Headers = "Action_If_Base_URL_Matches_Remove_From_Headers";
    private static final String Action_If_Base_URL_Matches_Header_Handle = "Action_If_Base_URL_Matches_";


    //丢弃请求的操作
    public static final String Action_Drop_Request_If_Host_Matches = "Action_Drop_Request_If_Host_Matches";
    public static final String Action_Drop_Request_If_URL_Matches = "Action_Drop_Request_If_URL_Matches";
    public static final String Action_Drop_Request_If_Keyword_Matches = "Action_Drop_Request_If_Keyword_Matches";
    private static final String Action_Drop_Request = "Action_Drop_Request";

    //自动放行的操作
    public static final String Action_Forward_Request_If_Host_Matches = "Action_Forward_Request_If_Host_Matches";
    public static final String Action_Forward_Request_If_URL_Matches = "Action_Forward_Request_If_URL_Matches";
    public static final String Action_Forward_Request_If_Keyword_Matches = "Action_Forward_Request_If_Keyword_Matches";

    private static final String Action_Forward_Request = "Action_Forward_Request";


    //配置
    public static final String Config_Custom_Payload = "Config_Custom_Payload";
    public static final String Config_Custom_Payload_Base64 = "Config_Custom_Payload_Base64";
    public static final String Config_Basic_Variable = "Config_Basic_Variable";
    public static final String Config_Chunked_Variable = "Config_Chunked_Variable";

    private static final String Config_ = "Config_";

    public static final String Run_External_Cmd = "Run_External_Cmd";

    public static final String Scope_Comment_Global = " This config affects ALL requests; ";
    public static final String Scope_Comment_checkbox = " The scope of this config is controlled by the checkbox above; ";

    public ConfigEntry() {
        //to resolve "default constructor not found" error
    }

    public ConfigEntry(String key, String value, String type, boolean enable) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.enable = enable;
        autoComment(type);
    }

    public ConfigEntry(String key, String value, String type, boolean enable, boolean editable) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.enable = enable;
        this.editable = editable;
        autoComment(type);
    }

    public ConfigEntry(String key, String value, String type, boolean enable, boolean editable, String comment) {
        this.key = key;
        this.value = value;
        this.type = type;
        this.enable = enable;
        this.editable = editable;
        this.comment = comment;
        autoComment(type);
    }

    public void autoComment(String type) {
        switch (type) {
            case Action_Add_Or_Replace_Header:
            case Action_Append_To_header_value:
                if (!comment.contains(Scope_Comment_checkbox)) {
                    this.comment = Scope_Comment_checkbox + this.comment;
                }
                break;
            case Action_Remove_From_Headers:
            case Action_Forward_And_Hide_Options:
                if (!comment.contains(Scope_Comment_Global)) {
                    this.comment = Scope_Comment_Global + this.comment;
                }
                break;
            default:
                this.comment = this.comment.replaceAll(Pattern.quote(Scope_Comment_checkbox), "");
                this.comment = this.comment.replaceAll(Pattern.quote(Scope_Comment_Global), "");
        }
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
        autoComment(type);
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String ToJson() {//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
        return new Gson().toJson(this);
    }

    public ConfigEntry FromJson(String json) {//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
        return new Gson().fromJson(json, ConfigEntry.class);
    }

    public boolean isConfigType() {
        if (type.startsWith(Config_)) {
            return true;
        }
        return false;
    }

    /**
     * 所有的action
     *
     * @return
     */
    public boolean isActionType() {
        if (type.startsWith(Action_)) {
            return true;
        }
        return false;
    }

    public boolean isDropOrForwardActionType() {
        if (type.startsWith(Action_Forward_Request)) {
            return true;
        }
        if (type.startsWith(Action_Drop_Request)) {
            return true;
        }
        return false;
    }

    public boolean isDropActionType() {
        if (type.startsWith(Action_Drop_Request)) {
            return true;
        }
        return false;
    }

    public boolean isHeaderHandleWithIfActionType() {
        if (type.startsWith(Action_If_Base_URL_Matches_Header_Handle)) {
            return true;
        }
        return false;
    }

    public String[] listAllDropForwardActions() {
        List<String> fieldList = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().startsWith(Action_Forward_Request) && Modifier.isPublic(f.getModifiers())) {
                fieldList.add(f.getName());
            }
            if (f.getName().startsWith(Action_Drop_Request) && Modifier.isPublic(f.getModifiers())) {
                fieldList.add(f.getName());
            }
        }
        fieldList.add("Show Help");
        String[] array = new String[fieldList.size()];
        fieldList.toArray(array); // fill the array
        return array;
    }

    public String[] listAllConfigType() {
        List<String> fieldList = new ArrayList<>();
        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getName().startsWith(Action_) && Modifier.isPublic(f.getModifiers())) {
                fieldList.add(f.getName());
            }
            if (f.getName().startsWith(Config_) && Modifier.isPublic(f.getModifiers())) {
                fieldList.add(f.getName());
            }
            if (f.getName().startsWith(Run_External_Cmd) && Modifier.isPublic(f.getModifiers())) {
                fieldList.add(f.getName());
            }
        }
        String[] array = new String[fieldList.size()];
        fieldList.toArray(array); // fill the array
        return array;
    }

    public boolean isInRuleScope(int toolFlag, IHttpRequestResponse messageInfo) {
        HelperPlus getter = BurpExtender.getHelperPlus();
        String baseUrl = HelperPlus.getBaseURL(messageInfo).toString();
        String url;
		try {
			url = getter.getFullURL(messageInfo).toString();
		} catch (Exception e) {
			url = baseUrl;
		}
        String host = HelperPlus.getHost(messageInfo);
        String configkey = getKey();

        switch (type) {
            case Action_Add_Or_Replace_Header:
            case Action_Append_To_header_value:
                return isInCheckBoxScope(toolFlag, messageInfo);

            case Action_Remove_From_Headers:
                return true;
            case Action_Forward_And_Hide_Options:
                String method = getter.getMethod(messageInfo);
                return method.equals("OPTIONS");

            //范围是URL的数据包修改
            case Action_If_Base_URL_Matches_Add_Or_Replace_Header:
            case Action_If_Base_URL_Matches_Append_To_header_value:
            case Action_If_Base_URL_Matches_Remove_From_Headers:
                return configkey.equalsIgnoreCase(baseUrl);

            //丢弃请求的操作//自动放行的操作
            case Action_Drop_Request_If_Host_Matches:
            case Action_Forward_Request_If_Host_Matches:
                if (configkey.startsWith("*.")) {
                    String tmpDomain = configkey.replaceFirst("\\*", "");
                    return host.toLowerCase().endsWith(tmpDomain.toLowerCase());
                } else {
                    return host.equalsIgnoreCase(configkey);
                }

            case Action_Drop_Request_If_URL_Matches:
            case Action_Forward_Request_If_URL_Matches:
                return url.equalsIgnoreCase(configkey);

            case Action_Drop_Request_If_Keyword_Matches:
            case Action_Forward_Request_If_Keyword_Matches:
                return url.contains(configkey);
        }
        return false;
    }

    public String getFinalValue(IHttpRequestResponse messageInfo) {
        IHttpRequestResponse[] messageInfos = {messageInfo};
        return getFinalValue(messageInfos);
    }

    public String getFinalValue(IHttpRequestResponse[] messageInfos) {
        String valueStr = getValue();
        if (StringUtils.isEmpty(valueStr)) {
            return valueStr;
        }

        List<String> items = TextUtils.grepWithRegex(valueStr, "\\{.*?\\}");

        List<String> httpParts = MessagePart.getPartList();
        List<ConfigEntry> varConfigs = GUI.configTableModel.getBasicConfigVars();

        for (String item : items) {
            String partType = item.replace("{", "").replace("}", "");
            for (String part : httpParts) {
                if (partType.equalsIgnoreCase(part)) {
                    String value = getValueByPartType(messageInfos, partType);
                    valueStr = valueStr.replace(item, value);
                }
            }
            for (ConfigEntry config : varConfigs) {
                if (partType.equalsIgnoreCase(config.getKey())) {
                    valueStr = valueStr.replace(item, config.getValue());
                }
            }
        }
        return valueStr;
    }

    public boolean ifNeedTakeAction(int toolFlag, IHttpRequestResponse messageInfo) {
        if (!isEnable()) return false;
        if (!isActionType()) return false;
        return isInRuleScope(toolFlag, messageInfo);
    }

    /**
     * 参数结构和processProxyMessage一致
     * public void processProxyMessage(boolean messageIsRequest, IInterceptedProxyMessage message)
     *
     * @param messageIsRequest
     * @param message
     */
    public void takeProxyAction(boolean messageIsRequest, IInterceptedProxyMessage message) {
        if (!ifNeedTakeAction(IBurpExtenderCallbacks.TOOL_PROXY, message.getMessageInfo())) return;

        IHttpRequestResponse messageInfo = message.getMessageInfo();
        HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());

        if (messageIsRequest) {
            switch (type) {
                //自动放行的操作，只在proxy中
                case Action_Forward_And_Hide_Options:
                case Action_Forward_Request_If_Host_Matches:
                case Action_Forward_Request_If_URL_Matches:
                case Action_Forward_Request_If_Keyword_Matches:
                    message.setInterceptAction(IInterceptedProxyMessage.ACTION_DONT_INTERCEPT);
                    message.getMessageInfo().setComment("Auto Forwarded by Knife");
                    message.getMessageInfo().setHighlight("gray");
                    break;

                //丢弃请求的操作，只在proxy中
                case Action_Drop_Request_If_Host_Matches:
                case Action_Drop_Request_If_URL_Matches:
                case Action_Drop_Request_If_Keyword_Matches:
                    message.setInterceptAction(IInterceptedProxyMessage.ACTION_DROP);
                    message.getMessageInfo().setComment("Auto Dropped by Knife");
                    message.getMessageInfo().setHighlight("gray");
                    //无需进行后面的数据包修改
                    return;
            }
        } else {
            switch (type) {
                case Action_Forward_And_Hide_Options:
                    getter.addOrUpdateHeader(messageIsRequest, messageInfo, "Content-Type", "application/octet-stream");
                    messageInfo.setComment("auto changed by knife");
            }
        }

        takeEditAction(IBurpExtenderCallbacks.TOOL_PROXY, messageIsRequest, messageInfo);

    }

    /**
     * 参数结构和processHttpMessage一致
     * public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo)
     *
     * @param toolFlag
     * @param messageIsRequest
     * @param messageInfo
     */
    public void takeEditAction(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        if (!ifNeedTakeAction(toolFlag, messageInfo)) return;

        byte[] oldRequest = messageInfo.getRequest();

        String configKey = getKey();
        String configValue = getFinalValue(messageInfo);

        HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());

        if (messageIsRequest) {//数据包自动修改
            switch (type) {
                case Action_Add_Or_Replace_Header:
                case Action_If_Base_URL_Matches_Add_Or_Replace_Header:
                    getter.addOrUpdateHeader(true, messageInfo, configKey, configValue);
                    //注意，单个分支应该break。
                    break;
                case Action_Append_To_header_value:
                case Action_If_Base_URL_Matches_Append_To_header_value:
                    String oldValue = getter.getHeaderValueOf(true, messageInfo, configKey);
                    if (oldValue == null) {
                        oldValue = "";
                    }
                    getter.addOrUpdateHeader(true, messageInfo, configKey, oldValue + configValue);
                    break;
                case Action_Remove_From_Headers:
                case Action_If_Base_URL_Matches_Remove_From_Headers:
                    getter.removeHeader(true, messageInfo, configKey);
                    break;
            }
        }

        byte[] newRequest = messageInfo.getRequest();

        if (!Arrays.equals(newRequest, oldRequest)) {
            //https://stackoverflow.com/questions/9499560/how-to-compare-the-java-byte-array
            messageInfo.setComment("auto changed by knife");
        }
    }
}
