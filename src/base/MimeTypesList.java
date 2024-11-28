package base;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypes;

public class MimeTypesList {
	 public static void main(String[] args) {
		 genMIMETypeListAsPathBlackList();
	 }
	
	/**
	 * 生成用于作为路径黑名单的MIME列表
	 * @param args
	 */
    public static List<String> genMIMETypeListAsPathBlackList() {
    	List<String> result = new ArrayList<>();
    	
        // 获取默认的 MimeTypes 实例
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();

        // 获取所有已注册的 MIME 类型
        for (MediaType type : allTypes.getMediaTypeRegistry().getTypes()) {
        	String typeStr = type.getType().toString();
        	//"text" for "text/plain"
        	String subTypeStr = type.getSubtype().toString();
        	//"plain" for "text/plain"
        	if (subTypeStr.contains(";")) {
        		subTypeStr = subTypeStr.substring(0,subTypeStr.indexOf(";")+1);
        	}
        	if (subTypeStr.contains("-")) {
        		subTypeStr = subTypeStr.substring(0,subTypeStr.indexOf("-")+1);
        	}
        	if (subTypeStr.contains(".")) {
        		subTypeStr = subTypeStr.substring(0,subTypeStr.indexOf(".")+1);
        	}
        	if (subTypeStr.contains("+")) {
        		subTypeStr = subTypeStr.substring(0,subTypeStr.indexOf("+")+1);
        	}
        	
            String item  =typeStr+"/"+subTypeStr;
            //System.out.println(item);
            
            if (!result.contains(item)) {
            	result.add(item);
            }
            result.add("text/");
        }
        
//        try {
//			FileUtils.writeLines(new File("blackPath.txt"), result);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
        
        return result;
    }
}
