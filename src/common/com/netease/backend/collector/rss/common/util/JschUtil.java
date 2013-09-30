package com.netease.backend.collector.rss.common.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

/**
 *
 */
public class JschUtil {

    private static JschUtil instance;

    public static JschUtil getInstance() {
        if (instance == null) {
            instance = new JschUtil();
        }
        return instance;
    }

    private JschUtil() {

    }

    private Session getSession(String host, int port, String ueseName)
            throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(System.getProperty("user.home") + "/.ssh/id_rsa");
        return jsch.getSession(ueseName, host, port);
    }

    public Session connect(String host, int port, String ueseName) throws Exception {
        Session session = getSession(host, port, ueseName);
        Properties config = new Properties();
        config.setProperty("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }

    /**
     * 执行命令，多条命令以分号分隔
     * @param session ssh session
     * @param command 命令
     * @return 执行返回字符串
     * @throws Exception 执行命令异常
     */
    public String execCmd(Session session, String command) throws Exception {
        if (session == null) {
            throw new RuntimeException("Session is null!");
        }
        ChannelExec exec = (ChannelExec) session.openChannel("exec");
        exec.setInputStream(null);

        exec.setCommand(command);
        exec.connect();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
        StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null){
            buffer.append(line);
            buffer.append("\n");
        }
        exec.disconnect();

        return buffer.toString();
    }

    public void clear(Session session) {
        if (session != null && session.isConnected()) {
            session.disconnect();
            session = null;
        }
    }

    public static void main(String[] args) throws Exception {
        Session session = JschUtil.getInstance().connect("app-59.photo.163.org", 1046, "dir");
        String cmd = "du -b jsj |awk '{print $1}'";
        String result = JschUtil.getInstance().execCmd(session, cmd);
        System.out.println(result);
        System.out.println("----");
        if (org.apache.commons.lang.StringUtils.isBlank(result))
            System.out.println("wo shi kong bai");
        long size = Long.valueOf(org.apache.commons.lang.StringUtils.trim(result));
        System.out.println(size);
        System.out.println("----");
        System.exit(0);
    }
}
