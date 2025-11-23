package messageTab.Info;

import com.google.gson.Gson;

public class InfoEntry {

    private String value = "";
    private String type = "";
    private boolean enable = true;
    private boolean editable = true;//whether you can edit name and type
    private String comment = "";
    
    public static final String Type_URL ="Type_URL";
    public static final String Type_Email ="Type_Email";
    public static final String Type_Domain ="Type_Domain";
    public static final String Type_IP ="Type_IP";

    public InfoEntry() {
        //to resolve "default constructor not found" error
    }
    
    public InfoEntry(String value, String type) {
        this.value = value;
        this.type = type;
        this.enable = true;
    }

    public InfoEntry(String value, String type, boolean enable) {
        this.value = value;
        this.type = type;
        this.enable = enable;
    }

    public InfoEntry(String value, String type, boolean enable, boolean editable) {
        this.value = value;
        this.type = type;
        this.enable = enable;
        this.editable = editable;
    }

    public InfoEntry(String value, String type, boolean enable, boolean editable, String comment) {
        this.value = value;
        this.type = type;
        this.enable = enable;
        this.editable = editable;
        this.comment = comment;
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

    public String ToJson() {//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
        return new Gson().toJson(this);
    }

    public InfoEntry FromJson(String json) {//注意函数名称，如果是get set开头，会被认为是Getter和Setter函数，会在序列化过程中被调用。
        return new Gson().fromJson(json, InfoEntry.class);
    }

}
