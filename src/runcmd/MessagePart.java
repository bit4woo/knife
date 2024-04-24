package runcmd;

import java.util.List;

public class MessagePart {

	public static final String Host = "Host";
	public static final String BaseURL = "BaseURL";
	public static final String FullURL = "FullURL";

	public static final String Request = "Request";
	public static final String Response = "Response";
	public static final String RequestHeaders = "RequestHeaders";
	public static final String RequestBody = "RequestHeaders";
	public static final String ResponseHeaders = "ResponseHeaders";
	public static final String ResponseBody = "ResponseBody";

	public static List<String> getPartList(){
		return ClassUtils.getPublicStaticFinalStringFields(MessagePart.class);
	}

	public MessagePart() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		System.out.println(getPartList());
	}
}
