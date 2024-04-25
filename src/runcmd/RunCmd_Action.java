package runcmd;

import burp.*;
import com.bit4woo.utilbox.utils.SystemUtils;
import config.ConfigEntry;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;


public class RunCmd_Action implements ActionListener, Runnable {

    public static final String workdir = System.getProperty("user.home") + File.separator + ".knife";
    private final IContextMenuInvocation invocation;
    private final ConfigEntry config;
    public IExtensionHelpers helpers;
    public PrintWriter stdout;
    public PrintWriter stderr;
    public IBurpExtenderCallbacks callbacks;
    public BurpExtender burp;


    public RunCmd_Action(BurpExtender burp, IContextMenuInvocation invocation, ConfigEntry config) {
        this.burp = burp;
        this.invocation = invocation;
        this.helpers = BurpExtender.helpers;
        this.callbacks = BurpExtender.callbacks;
        this.stderr = BurpExtender.getStderr();
        this.stdout = BurpExtender.getStdout();
        this.config = config; //是否使用多个数据包的内容
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        new Thread(this).start();//就是调用Runnable的run函数
    }

    @Override
    public void run() {
        try {
            IHttpRequestResponse[] messages = invocation.getSelectedMessages();
            if (messages != null) {
                boolean useRobot = (BurpExtender.tableModel.getConfigValueByKey("RunTerminalWithRobotInput") != null);
                if (useRobot) {
                    RobotInput.startCmdConsole();//尽早启动减少出错概率
                }

                String cmd = config.getFinalValue(messages);
                if (useRobot) {
                    //方案1：使用模拟输入实现
                    new RobotInput().inputString(cmd);
                } else {
                    //方案2：使用bat文件实现
                    String file = SystemUtils.genBatchFile(cmd, config.getKey() + ".bat");
                    SystemUtils.runBatchFile(file);
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace(stderr);
        }
    }
}