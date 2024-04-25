package knife;

import burp.BurpExtender;
import config.ConfigEntry;
import config.GUI;
import runcmd.RunCmd_Action;

import javax.swing.*;
import java.util.List;

public class RunCmdMenu extends JMenu {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    //JMenuItem vs. JMenu
    public RunCmdMenu(BurpExtender burp) {
        try {
            this.setText("^_^ Run Cmd");
            List<ConfigEntry> configs = GUI.tableModel.getConfigByType(ConfigEntry.Run_External_Cmd);

            for (ConfigEntry config : configs) {
                JMenuItem item = new JMenuItem(config.getKey());
                item.addActionListener(new RunCmd_Action(burp, burp.invocation, config));
                add(item);
            }

        } catch (Exception e) {
            e.printStackTrace(BurpExtender.getStderr());
        }
    }

    public static void main(String[] args) {

    }
}
