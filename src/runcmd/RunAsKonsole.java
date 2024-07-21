package runcmd;

import java.io.IOException;

public class RunAsKonsole {

    /**
     * 启动Konsole并异步执行命令
     * @param command 要执行的命令
     */
    public static void launchKonsoleAsync(String command) {
        Thread konsoleThread = new Thread(() -> {
            try {
                // 构建ProcessBuilder
                ProcessBuilder processBuilder = new ProcessBuilder("/usr/bin/konsole","--noclose", "-e", command);
                // 启动进程
                Process process = processBuilder.start();
                // 等待进程结束
                int exitCode = process.waitFor();

                // 进程结束后打印信息
                System.out.println("Konsole process exited with code: " + exitCode);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        // 设置线程为守护线程，这样在主程序退出时不会阻塞
        konsoleThread.setDaemon(true);
        // 启动线程
        konsoleThread.start();
    }
}
