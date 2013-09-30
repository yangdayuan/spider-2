/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

/**
 * 系统环境和进程相关参数工具类
 * @author wuliufu
 *
 */
public class EnvUtil {
	private static int cpuNum = 1;
	static {
		cpuNum = Math.max(1, Runtime.getRuntime().availableProcessors());
	}
	
	public static int cpuNum() {
		return cpuNum;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(cpuNum());
	}

}
