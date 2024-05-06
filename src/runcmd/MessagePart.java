package runcmd;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.ClassUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class MessagePart {

    public static final String Host = "Host";
    public static final String MultiHost = "MultiHost";
    public static final String HostPort = "HostPort";
    public static final String MultiHostPort = "MultiHostPort";
    public static final String BaseURL = "BaseURL";
    public static final String MultiBaseURL = "MultiBaseURL";
    public static final String FullURL = "FullURL";
    public static final String MultiFullURL = "MultiFullURL";

    public static final String Request = "Request";
    public static final String MultiRequest = "MultiRequest";
    public static final String RequestAsFile = "RequestAsFile";
    public static final String MultiRequestAsFile = "MultiRequestAsFile";
    public static final String Response = "Response";
    public static final String MultiResponse = "MultiResponse";
    public static final String ResponseAsFile = "ResponseAsFile";
    public static final String MultiResponseAsFile = "MultiResponseAsFile";
    public static final String RequestHeaders = "RequestHeaders";
    public static final String MultiRequestHeaders = "MultiRequestHeaders";
    public static final String RequestBody = "RequestBody";
    public static final String MultiRequestBody = "MultiRequestBody";
    public static final String ResponseHeaders = "ResponseHeaders";
    public static final String MultiResponseHeaders = "MultiResponseHeaders";
    public static final String ResponseBody = "ResponseBody";
    public static final String MultiResponseBody = "MultiResponseBody";
    
    
    private static final String ConnectingCharacter =" ";
    private static final String workdir = System.getProperty("user.home") + File.separator + ".knife";

    public static List<String> getPartList() {
        return ClassUtils.getPublicStaticFinalStringFields(MessagePart.class);
    }

    public static List<String> getMultiPartList() {
        List<String> items = ClassUtils.getPublicStaticFinalStringFields(MessagePart.class);
        Iterator<String> it = items.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (!item.startsWith("Multi")) {
                it.remove();
            }
        }
        return items;
    }

    /**
     * 消除大小写差异
     *
     * @param partType
     * @return
     */
    private static String getPartType(String partType) {
        for (String part : getPartList()) {
            if (partType.equalsIgnoreCase(part)) {
                return part;
            }
        }
        return "unknow";
    }

    private static String getPartOfMessage(IHttpRequestResponse message, String partType) {
        HelperPlus getter = new HelperPlus(BurpExtender.getCallbacks().getHelpers());
        partType = getPartType(partType);
        String value;
        switch (partType) {
            case Host:
            case MultiHost:
                value = HelperPlus.getHost(message);
                break;
            case HostPort:
            case MultiHostPort:
                value = HelperPlus.getHost(message) + ":" + HelperPlus.getPort(message);
                break;
            case BaseURL:
            case MultiBaseURL:
                value = HelperPlus.getBaseURL(message).toString();
                break;
            case FullURL:
            case MultiFullURL:
                value = getter.getFullURL(message).toString();
                break;
            case Request:
            case MultiRequest:
            case RequestAsFile:
            case MultiRequestAsFile:
                value = new String(message.getRequest());
                break;
            case Response:
            case MultiResponse:
            case ResponseAsFile:
            case MultiResponseAsFile:
                value = new String(message.getResponse());
                break;
            case RequestHeaders:
            case MultiRequestHeaders:
                value = getter.getHeadersAsStr(true, message);
                break;
            case ResponseHeaders:
            case MultiResponseHeaders:
                value = getter.getHeadersAsStr(false, message);
                break;
            case RequestBody:
            case MultiRequestBody:
                value = new String(HelperPlus.getBody(true, message));
                break;
            case ResponseBody:
            case MultiResponseBody:
                value = new String(HelperPlus.getBody(false, message));
                break;
            default:
                value = "NotValidPartType";
        }
        return value;
    }


    /**
     * 多个数据包的内容作为一个整体或者写入一个文件返回。
     *
     * @param messages
     * @param partType
     * @return
     */
    public static String getValueByPartType(IHttpRequestResponse[] messages, String partType) {
        List<String> tempValues = new ArrayList<>();
        String firstHost = "";
        for (IHttpRequestResponse message : messages) {
            if (StringUtils.isEmpty(firstHost)) {
                firstHost = HelperPlus.getHost(message);
            }
            String value = getPartOfMessage(message, partType);
            tempValues.add(value);
            if (!partType.toLowerCase().startsWith("multi")) {
                break;
            }
        }
        
        if (partType.toLowerCase().endsWith("asfile")) {
            if (tempValues.size() > 1) {
                firstHost = firstHost + "." + tempValues.size();
            }
            String content = String.join(System.lineSeparator(), tempValues);
            return contentToFile(firstHost, content);
        } else {
        	String content = String.join(ConnectingCharacter, tempValues);
            return content;
        }
    }

    public static String getValueByPartType(IHttpRequestResponse message, String partType) {
        IHttpRequestResponse[] array = {message};
        return getValueByPartType(array, partType);
    }


    public static String contentToFile(String hostOfFilename, String content) {
        if (StringUtils.isEmpty(hostOfFilename) || StringUtils.isEmpty(content)) {
            return null;
        }
        try {
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("MMdd-HHmmss");
            String timeString = simpleDateFormat.format(new Date());
            String filename = hostOfFilename + "." + timeString + ".req";

            File requestFile = new File(workdir, filename);
            FileUtils.writeStringToFile(requestFile, content, "UTF-8");
            return requestFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace(BurpExtender.getStderr());
            return null;
        }
    }

    public MessagePart() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        System.out.println(getPartList());
        System.out.println(contentToFile("1111", "1111"));
    }
}
