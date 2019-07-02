package burp;

public class Utils {
    public static boolean isWindows() {
    	String OS_NAME = System.getProperties().getProperty("os.name").toLowerCase();
        if (OS_NAME.contains("windows")) {
            return true;
        } else {
        	return false;
        }
    }
}
