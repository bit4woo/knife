package runcmd;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import com.bit4woo.utilbox.burp.HelperPlus;
import com.bit4woo.utilbox.utils.ClassUtils;

import java.util.List;

public class MessagePart {

    public static final String Host = "Host";
    public static final String HostPort = "HostPort";
    public static final String BaseURL = "BaseURL";
    public static final String FullURL = "FullURL";

    public static final String Request = "Request";
    public static final String Response = "Response";
    public static final String RequestHeaders = "RequestHeaders";
    public static final String RequestBody = "RequestBody";
    public static final String ResponseHeaders = "ResponseHeaders";
    public static final String ResponseBody = "ResponseBody";

    public static List<String> getPartList() {
        return ClassUtils.getPublicStaticFinalStringFields(MessagePart.class);
    }


    public static String getValueByPartType(boolean isRequest, IHttpRequestResponse message, String partType) {
        HelperPlus getter = new HelperPlus(BurpExtender.getCallbacks().getHelpers());
        switch (partType) {
            case Host:
                return HelperPlus.getHost(message);
            case HostPort:
                return HelperPlus.getHost(message)+":"+HelperPlus.getPort(message);
            case BaseURL:
                return HelperPlus.getBaseURL(message).toString();
            case FullURL:
                return getter.getFullURL(message).toString();
            case Request:
                return new String(message.getRequest());
            case Response:
                return new String(message.getResponse());
            case RequestHeaders:
            case ResponseHeaders:
                return getter.getHeadersAsStr(isRequest, message);
            case RequestBody:
            case ResponseBody:
                return new String(HelperPlus.getBody(isRequest, message));
        }
        return "NotValidPart";
    }

    public MessagePart() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) {
        System.out.println(getPartList());
    }
}
