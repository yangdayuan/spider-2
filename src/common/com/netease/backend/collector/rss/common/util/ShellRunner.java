/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * shell执行工具类
 * @author wuliufu
 */
public class ShellRunner {
	private static final Logger logger = LoggerFactory.getLogger(ShellRunner.class);
	
	/**
	 * 检查执行的系统是否是linux/unix系列
	 * @return 是返回true，否则false
	 */
	public static boolean checkUnix() {
		String osName = System.getProperty("os.name").toLowerCase();
		if("linux".equals(osName) || "solaris".equals(osName) || "aix".equals(osName)) {
			return true;
		}
		return false;
	}

    /**
     * 检查是否是window系统
     * @return 是返回true，否则false
     */
    public static boolean checkWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.startsWith("windows");
    }
    
	/**
	 * 执行shell脚本
	 * @param shellPath shell文件所在路径
	 * @return 执行结果状态
	 * @throws IOException
	 */
	public static CommandResult runShell(String shellPath) throws IOException {
		return runShell(shellPath, 0L);
	}
	
	/**
	 * 执行shell脚本
	 * @param shell shell文件所在路径
     * @param timeout 超时毫秒数
	 * @return 执行结果状态
	 * @throws IOException
	 */
	public static CommandResult runShell(String shell, long timeout) throws IOException {
		if(!checkUnix()) {
			return null;
		}
		
		 long start = System.currentTimeMillis();
        Runtime rt = Runtime.getRuntime();  
        Process pcs= rt.exec(shell);
        CommandResult cr = process(pcs, timeout); 
        long end = System.currentTimeMillis();
        logger.info("Process {} with result value is {} and costs {} ms.", new Object[]{shell, cr.code, (end - start)});
    	return cr;
	}

	/**
	 * 支持windows和linux命令执行
	 * @param command
	 * @return
	 * @throws IOException
	 */
    public static CommandResult runCommand(String command) throws IOException {
        return runCommand(command, 0L);
    }
    
    /**
     * 支持windows和linux命令执行
     * @param command 命令, 如 svn export https://...
     * @param timeout 超时毫秒数
     * @return
     * @throws IOException
     */
    public static CommandResult runCommand(String command, long timeout) throws IOException {
        String[] cmd = new String[] {"cmd", "/C", command};
        if (checkUnix()) {
            cmd = new String[]{"/bin/sh", "-c", command};
        }
        
        long start = System.currentTimeMillis();
        Runtime rt = Runtime.getRuntime();
        Process pcs = rt.exec(cmd);   	
        CommandResult cr = process(pcs, timeout); 
        long end = System.currentTimeMillis();
        logger.info("Process {} with result value is {} and costs {} ms.", new Object[]{command, cr.code, (end - start)});
    	return cr;
    }
    
    private static CommandResult process(Process pcs, long timeout) {
        ProcessReader in = new ProcessReader(pcs.getInputStream());
        in.setDaemon(true);
        in.start();
        ProcessReader err = new ProcessReader(pcs.getErrorStream());
        err.setDaemon(true);
        err.start();
        Integer exitValue = null;
    	ProcessWorker pw = new ProcessWorker(pcs);
    	pw.start();
    	try {
    		if(timeout <= 0) {
    			pw.join();
    		} else {
    			pw.join(timeout);
    		}
			exitValue = pw.getExitValue();
		} catch (InterruptedException e) {
			pw.interrupt();
		}
    	
        CommandResult result = null;
    	if(exitValue == null) {
    		result = new CommandResult(CommandResult.EXIT_VALUE_TIMEOUT, in.resultString(), err.resultString());
    	} else {
    		result = new CommandResult((int)(exitValue.longValue()), in.resultString(), err.resultString());
    	}
    	
    	return result;
    }
    
    public static class ProcessWorker extends Thread {
    	private Process pcs;
    	private Integer exitValue;
    	
		public ProcessWorker(Process pcs) {
			this.pcs = pcs;
		}
		
		public void run() {
			try {
				exitValue = pcs.waitFor();
			} catch(InterruptedException e) {
				logger.error(e.getMessage());
			} finally {
				try {
					pcs.destroy();
				} catch(Throwable t) {
					logger.error("", t);
				}
			}
		}

		/**
		 * 获取exitValue
		 * @return exitValue exitValue
		 */
		public Integer getExitValue() {
			return exitValue;
		}
		
    }
    
    /**
     * 命令执行结果
     * @author wuliufu
     *
     */
    public static class CommandResult {
    	public static int EXIT_VALUE_TIMEOUT = -99;
    	private int code;
    	private String outMessage;
    	private String errorMessage;
		public CommandResult() {
		}
		public CommandResult(int code, String outMessage, String errorMessage) {
			this.code = code;
			this.outMessage = outMessage;
			this.errorMessage = errorMessage;
		}
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public String getOutMessage() {
			return outMessage;
		}
		public void setOutMessage(String outMessage) {
			this.outMessage = outMessage;
		}
		public String getErrorMessage() {
			return errorMessage;
		}
		public void setErrorMessage(String errorMessage) {
			this.errorMessage = errorMessage;
		}
    	
    }
    
    private static class ProcessReader extends Thread {
    	private InputStream in;
    	private StringBuilder builder = null;
    	
    	private ProcessReader(InputStream in) {
			this.in = in;
		}

		public void run() {
			BufferedReader br = null;
			builder = new StringBuilder(1024);
			try {
				br = new BufferedReader(new InputStreamReader(in));
		        String line;
		        while ((line = br.readLine()) != null) {
		        	logger.info(line);
		        	builder.append(line);
		        }
			} catch(Exception e) {
				logger.error("", e);
			} finally {
				if(br != null) {
					try {
						br.close();
						in.close();
					} catch (IOException e) {
						logger.error("", e);
					}
				}
			} 
	        
    	}
		
		public String resultString() {
			if(builder == null) {
				return null;
			}
			return builder.toString();
		}
    }
    
    public static void main(String[] args) {
    	try {
    		CommandResult r = runCommand("wkhtmltoimage --version", 3000);
    		System.out.println(r.code);
    		System.out.println(r.outMessage);
    		System.err.println(r.errorMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
