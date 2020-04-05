package config;

import com.google.gson.Gson;

public class ConfigEntry {
	
	private String key = "";
	private String value = "";
	private String type ="";
	private boolean enable = true;
	private boolean editable = true;//whether can edit name and type
	
	public static final String Action_Add_Or_Replace_Header = "Action_Add_Or_Replace_Header";
	public static final String Action_Append_To_header_value = "Action_Append_To_header_value";
	public static final String Action_Remove_From_Headers = "Action_Remove_From_Headers";
	
	public static final String Config_Custom_Payload = "Config_Custom_Payload";
	public static final String Config_Custom_Payload_Base64 = "Config_Custom_Payload_Base64";
	public static final String Config_Basic_Variable = "Config_Basic_Variable";
	public static final String Config_Chunked_Variable = "Config_Chunked_Variable";
	public static final String Config_Proxy_Variable = "Config_Proxy_Variable";
    
	ConfigEntry(){
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

	public String ToJson(){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().toJson(this);
	}
	
	public ConfigEntry FromJson(String json){//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
		return new Gson().fromJson(json, ConfigEntry.class);
	}

}
