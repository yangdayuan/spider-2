/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

/**
 * @author wuliufu
 */
public class BitUtil {
	/**
	 * 把num的第k位置0
	 * @param num 
	 * @param k
	 * @return
	 */
	public static int clear(int num, int k) {
		return num = num&~(1<<k);
	}
	
	/**
	 * 把num的第k位置1
	 * @param num 
	 * @param k
	 * @return
	 */
	public static int set(int num, int k) {
		return num = num |(1<<k);
	}
	
	/**
	 * 取数值的第k位的值
	 * @param num
	 * @param k
	 * @return
	 */
	public static int get(int num, int k) {
		return num>>k&1;
	}
}
