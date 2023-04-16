package config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class ConfigEntry {

	private String key = "";
	private String value = "";
	private String type ="";
	private boolean enable = true;
	private boolean editable = true;//whether can edit name and type
	private String comment = "";

	public static final String Action_Add_Or_Replace_Header = "Action_Add_Or_Replace_Header";// scope is controlled by gui
	public static final String Action_Append_To_header_value = "Action_Append_To_header_value";// scope is controlled by gui
	public static final String Action_Remove_From_Headers = "Action_Remove_From_Headers"; //scope is for all request
	private static final String Action_ = "Action_";

	public static final String Action_If_Base_URL_Matches_Add_Or_Replace_Header = "Action_If_Base_URL_Matches_Add_Or_Replace_Header";
	public static final String Action_If_Base_URL_Matches_Append_To_header_value = "Action_If_Base_URL_Matches_Append_To_header_value";
	public static final String Action_If_Base_URL_Matches_Remove_From_Headers = "Action_If_Base_URL_Matches_Remove_From_Headers";
	private static final String Action_If_Base_URL_Matches_Header_Handle = "Action_If_Base_URL_Matches_";


	public static final String Action_Drop_Request_If_Host_Matches = "Action_Drop_Request_If_Host_Matches";
	public static final String Action_Drop_Request_If_URL_Matches = "Action_Drop_Request_If_URL_Matches";
	public static final String Action_Drop_Request_If_Keyword_Matches = "Action_Drop_Request_If_Keyword_Matches";
	private static final String Action_Drop_Request = "Action_Drop_Request";

	public static final String Action_Forward_Request_If_Host_Matches = "Action_Forward_Request_If_Host_Matches";
	public static final String Action_Forward_Request_If_URL_Matches = "Action_Forward_Request_If_URL_Matches";
	public static final String Action_Forward_Request_If_Keyword_Matches = "Action_Forward_Request_If_Keyword_Matches";
	private static final String Action_Forward_Request = "Action_Forward_Request";


	public static final String Config_Custom_Payload = "Config_Custom_Payload";
	public static final String Config_Custom_Payload_Base64 = "Config_Custom_Payload_Base64";
	public static final String Config_Basic_Variable = "Config_Basic_Variable";
	public static final String Config_Chunked_Variable = "Config_Chunked_Variable";
	public static final String Config_Proxy_Variable = "Config_Proxy_Variable";

	private static final String Config_ = "Config_";


	public ConfigEntry(){
		//to resolve "default constructor not found" error
	}

	public ConfigEntry(String key,String value,String type,boolean enable){
		this.key = key;
		this.value = value;
		this.type = type;
		this.enable = enable;
	}

	public ConfigEntry(String key,String value,String type,boolean enable,boolean editable){
		this.key = key;
		this.value = value;
		this.type = type;
		this.enable = enable;
		this.editable = editable;
	}

	public ConfigEntry(String key,String value,String type,boolean enable,boolean editable,String comment){
		this.key = key;
		this.value = value;
		this.type = type;
		this.enable = enable;
		this.editable = editable;
		this.comment = comment;
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

	public String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().toJson(this);
	}

	public ConfigEntry FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
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

	public boolean isForwardActionType() {
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

	public boolean isScopeBasedHeaderHandleActionType() {
		if (type.equals(Action_Add_Or_Replace_Header)) {
			return true;
		}
		if (type.equals(Action_Append_To_header_value)) {
			return true;
		}
		if (type.equals(Action_Remove_From_Headers)) {
			return true;
		}
		return false;
	}


	public String[] listAllDropForwardActions() {
		List<String> fieldList = new ArrayList<String>();
		Field[] fields = getClass().getDeclaredFields();
		for (Field f:fields) {
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
}
