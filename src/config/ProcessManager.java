package config;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.DomainUtils;
import com.bit4woo.utilbox.utils.UrlUtils;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;

public class ProcessManager {

    //////////////////////////////////////////common methods for cookie handle///////////////////////////////

    //目标httpservice 和 headerLine
    private static String usedCookieOfUpdate = null;

    public static String getUsedCookieOfUpdate() {
        return usedCookieOfUpdate;
    }

    public static void setUsedCookieOfUpdate(String usedCookieOfUpdate) {
        ProcessManager.usedCookieOfUpdate = usedCookieOfUpdate;
    }


    public static String fetchUsedCookieAsTips() {
        String cookieValue = usedCookieOfUpdate.split(":")[1].trim();
        if (cookieValue.length() <= 20) {
            return cookieValue;
        } else {
            return cookieValue.substring(0, 20) + "...";
        }
    }

    @Deprecated
    public static IHttpRequestResponse[] Reverse(IHttpRequestResponse[] input) {
        for (int start = 0, end = input.length - 1; start < end; start++, end--) {
            IHttpRequestResponse temp = input[end];
            input[end] = input[start];
            input[start] = temp;
        }
        return input;
    }


    public static IHttpRequestResponse updateCookie(boolean messageIsRequest, IHttpRequestResponse messageInfo, String cookieValue) {
        return updateHeader(messageIsRequest, messageInfo, cookieValue);
    }


    public static IHttpRequestResponse updateHeader(boolean messageIsRequest, IHttpRequestResponse messageInfo, String headerLine) {
        try {
            HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
            String headerName = headerLine.split(":")[0].trim();
            String headerValue = headerLine.split(":")[1].trim();
            return getter.addOrUpdateHeader(messageIsRequest, messageInfo, headerName, headerValue);
        } catch (Exception e) {
            e.printStackTrace(BurpExtender.getStderr());
            return messageInfo;
        }
    }

    public static void addHandleRule(IHttpRequestResponse[] messages, String headerLine) {
        for (IHttpRequestResponse message : messages) {
            String targetShortUrl = HelperPlus.getBaseURL(message).toString();
            ConfigEntry rule = new ConfigEntry(targetShortUrl, headerLine, ConfigEntry.Action_If_Base_URL_Matches_Add_Or_Replace_Header, true);
            delSameConditionRule(rule);
            GUI.configTableModel.addNewConfigEntry(rule);
            BurpExtender.getStdout().println("new handle rule added: " + targetShortUrl + " : " + headerLine);
        }
    }

    /**
     * 删除key、type完全相同，value的header相同的rule
     *
     * @param newrule
     */
    public static void delSameConditionRule(ConfigEntry newRule) {
        List<ConfigEntry> rules = GetHeaderHandleWithIfRules();
        for (int i = rules.size() - 1; i >= 0; i--) {
            ConfigEntry rule = rules.get(i);
            if (rule.getKey().equals(newRule.getKey()) &&
                    rule.getType().equals(newRule.getType())) {
                String header = rule.getValue().split(":")[0];
                String newHeader = newRule.getValue().split(":")[0];
                if (header.equals(newHeader)) {
                    GUI.configTableModel.removeConfigEntry(rule);
                }
            }
        }
    }

    public static List<ConfigEntry> GetHeaderHandleWithIfRules() {
        List<ConfigEntry> result = new ArrayList<>();

        List<ConfigEntry> entries = GUI.configTableModel.getConfigEntries();
        for (ConfigEntry entry : entries) {
            if (entry.isHeaderHandleWithIfActionType()) {
                if (entry.isEnable()) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    /**
     * 获取所有对数据包进行修改的规则，除了drop和forward规则。
     *
     * @return
     */
    public static List<ConfigEntry> getEditActionRules() {
        List<ConfigEntry> result = new ArrayList<>();
        List<ConfigEntry> entries = GUI.configTableModel.getConfigEntries();
        for (ConfigEntry entry : entries) {
            if (entry.isActionType()) {
                if (!entry.isDropOrForwardActionType()) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    /**
     * 修改规则前后，都应该和GUI同步
     *
     * @param messages
     * @param action
     */
    public static void putDismissRule(IHttpRequestResponse[] messages, String keyword, String action) {
        if (messages != null) {
            HelperPlus getter = new HelperPlus(BurpExtender.getCallbacks().getHelpers());
            for (IHttpRequestResponse message : messages) {
                String configKey = null;
                switch (action) {
                    case ConfigEntry.Action_Forward_Request_If_Host_Matches:
                    case ConfigEntry.Action_Drop_Request_If_Host_Matches:
                        configKey = HelperPlus.getHost(message);
                        break;

                    case ConfigEntry.Action_Forward_Request_If_URL_Matches:
                    case ConfigEntry.Action_Drop_Request_If_URL_Matches:
                        configKey = getter.getFullURL(message).toString();
                        break;

                    case ConfigEntry.Action_Forward_Request_If_Keyword_Matches:
                    case ConfigEntry.Action_Drop_Request_If_Keyword_Matches:
                        configKey = keyword;
                        break;
                }
                if (StringUtils.isNotEmpty(configKey)) {
                    delSameConditionRule(configKey);
                    GUI.configTableModel.addNewConfigEntry(new ConfigEntry(configKey, "", action, true));
                }
            }
        }
    }

    /**
     * @param messages
     */
    public static void removeDismissRule(int toolFlag, IHttpRequestResponse[] messages) {
        for (IHttpRequestResponse message : messages) {
            List<ConfigEntry> rules = GetAllDropOrForwardRules();
            for (ConfigEntry rule : rules) {
                if (rule.ifNeedTakeAction(toolFlag, message)) {
                    GUI.configTableModel.removeConfigEntry(rule);
                }
            }
        }
    }

    /**
     * 判断是否存在相同条件的规则，如果存在应当删除旧的规则
     */
    public static void delSameConditionRule(String configKey) {
        List<ConfigEntry> rules = GetAllDropOrForwardRules();
        for (int i = rules.size() - 1; i >= 0; i--) {
            ConfigEntry rule = rules.get(i);
            if (rule.getKey().equals(configKey)) {
                GUI.configTableModel.removeConfigEntry(rule);
            }
        }
    }

    public static List<ConfigEntry> GetAllDropOrForwardRules() {
        List<ConfigEntry> result = new ArrayList<>();
        List<ConfigEntry> entries = GUI.configTableModel.getConfigEntries();
        for (ConfigEntry entry : entries) {
            if (entry.isDropOrForwardActionType()) {
                if (entry.isEnable()) {
                    result.add(entry);
                }
            }
        }
        return result;
    }

    /**
     * 获取所有drop规则，可以先处理这些规则。
     *
     * @return
     */
    public static List<ConfigEntry> getAllActionRules() {
        List<ConfigEntry> result = new ArrayList<>();
        List<ConfigEntry> entries = GUI.configTableModel.getConfigEntries();
        for (ConfigEntry entry : entries) {
            if (entry.isActionType() && entry.isEnable()) {
                result.add(entry);
            }
        }
        return result;
    }


    public static void doChunk(boolean messageIsRequest, IHttpRequestResponse message) {

        ConfigEntry rule = GUI.configTableModel.getConfigByKey("Chunked-AutoEnable");

        if (rule != null) {
            HelperPlus getter = new HelperPlus(BurpExtender.callbacks.getHelpers());
            getter.addOrUpdateHeader(messageIsRequest, message, "Transfer-Encoding", " chunked");
            byte[] oldBody = getter.getBody(messageIsRequest, message);
            try {
                boolean useComment = false;
                if (GUI.configTableModel.getConfigValueByKey("Chunked-UseComment") != null) {
                    useComment = true;
                }
                String lenStr = GUI.configTableModel.getConfigValueByKey("Chunked-Length");
                int len = 10;
                if (lenStr != null) {
                    len = Integer.parseInt(lenStr);
                }
                byte[] body = Methods.encoding(oldBody, len, useComment);
                getter.UpdateBody(messageIsRequest, message, body);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace(BurpExtender.getStderr());
            }
        }
    }

}
