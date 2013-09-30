/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

/**
 * 参数对类
 * @author wuliufu
 */
public class Parameter<K, V> {
	private K key;
	private V value;
	public Parameter(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	/**
	 * key=value
	 */
	public String toString() {
		return key + "=" + value;
	}
}
