/**
 * 
 */
package com.netease.backend.collector.rss.common.util.identify;

/**
 * @author wuliufu
 */
public interface IIdentifyGenerator<T> {
	public String generate(T obj);
}
