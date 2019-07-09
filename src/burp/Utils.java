package burp;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;

public class Utils {
    public static boolean isWindows() {
    	String OS_NAME = System.getProperties().getProperty("os.name").toLowerCase();
        if (OS_NAME.contains("windows")) {
            return true;
        } else {
        	return false;
        }
    }
    
    
    public static void browserOpen(Object url,String browser) throws Exception{
		String urlString = null;
		URI uri = null;
		if (url instanceof String) {
			urlString = (String) url;
			uri = new URI((String)url);
		}else if (url instanceof URL) {
			uri = ((URL)url).toURI();
			urlString = url.toString();
		}
		if(browser == null ||browser.equalsIgnoreCase("default") || browser.equalsIgnoreCase("")) {
			//whether null must be the first
			Desktop desktop = Desktop.getDesktop();
			if(Desktop.isDesktopSupported()&&desktop.isSupported(Desktop.Action.BROWSE)){
				desktop.browse(uri);
			}
		}else {
			Runtime runtime = Runtime.getRuntime();
			runtime.exec(browser+" "+urlString);
			//C:\Program Files\Mozilla Firefox\firefox.exe
			//C:\\Program Files (x86)\\Mozilla Firefox\\firefox.exe
		}
	}
}
