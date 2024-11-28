package messageTab.Info;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class InfoPanelHeadPanel extends JPanel {
	
	JLabel baseUrllabelKey = new JLabel("Base URL: ");
	JLabel baseUrllabelValue = new JLabel("");
	
	public InfoPanelHeadPanel(){
	    this.setLayout(new FlowLayout(FlowLayout.CENTER));
	    this.add(baseUrllabelKey);
	    this.add(baseUrllabelValue);
	    
	}
	
	public void setBaseUrl(String url) {
		if (url!=null) {
			baseUrllabelValue.setText(url);
		}
	}

}
