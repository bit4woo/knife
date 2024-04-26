package knife;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import burp.Methods;
import config.ConfigEntry;
import config.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author bit4woo
 */
public class CustomPayloadMenu extends JMenu {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public BurpExtender myburp;

    public CustomPayloadMenu(BurpExtender burp) {
        try {
            this.setText("^_^ Insert Payload");
            this.myburp = burp;

            List<ConfigEntry> configs = GUI.getConfigTableModel().getConfigByType(ConfigEntry.Config_Custom_Payload);
            List<ConfigEntry> configs1 = GUI.getConfigTableModel().getConfigByType(ConfigEntry.Config_Custom_Payload_Base64);
            configs.addAll(configs1);
            for (ConfigEntry config : configs) {
                String name = config.getKey();
                JMenuItem item = new JMenuItem(name);
                item.addActionListener(new CustomPayloadItemListener(burp, config));
                add(item);
            }
        } catch (Exception e) {
            e.printStackTrace(BurpExtender.getStderr());
        }
    }
}

class CustomPayloadItemListener implements ActionListener {

    BurpExtender myburp;
    private ConfigEntry config;

    CustomPayloadItemListener(BurpExtender burp, ConfigEntry config) {
        this.myburp = burp;
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        IHttpRequestResponse req = myburp.invocation.getSelectedMessages()[0];
        byte[] request = req.getRequest();
        int[] selectedIndex = myburp.invocation.getSelectionBounds();

        byte[] newRequest = GetNewRequest(request, selectedIndex, config);
        req.setRequest(newRequest);
    }

    public byte[] GetNewRequest(byte[] request, int[] selectedIndex, ConfigEntry config) {//action is the payload name

        byte[] payloadBytes;
        if (config.getType().equals(ConfigEntry.Config_Custom_Payload_Base64)) {
            payloadBytes = Base64.getDecoder().decode(config.getValue());
        } else {
            String payload = config.getFinalValue(myburp.invocation.getSelectedMessages()[0]);
            payloadBytes = payload.getBytes();
        }

        if (payloadBytes != null) {
            return Methods.do_modify_request(request, selectedIndex, payloadBytes);
        } else {
            return request;
        }
    }
}
