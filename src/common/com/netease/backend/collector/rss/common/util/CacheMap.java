/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 简单缓存Map
 * @author wuliufu
 * @since 2011-08-18
 * @version 1.0.0
 * 
 */
public class CacheMap<K, V> implements Serializable {
	private static final long serialVersionUID = -6573917364455279961L;
	protected Map<K, Value> map = new HashMap<K, Value>();
	private int defaultExpire = 0;
	
	/**
	 * 获取全局缓存失效时间
	 * @return 失效时间，单位秒
	 */
	public int getDefaultExpire() {
		return defaultExpire;
	}

	/**
	 * 设置全局缓存失效时间
	 * @param defaultExpire 失效时间，单位秒
	 */
	public void setDefaultExpire(int defaultExpire) {
		this.defaultExpire = defaultExpire;
	}

	/**
	 * 添加一对key/value键值对
	 * @param key 键
	 * @param value 值
	 * @param expire 失效时间，单位秒
	 */
	public void put(K key, V value, int expire) {
		if(key == null)
			return;
		Value v = new Value();
		v.expireTime = System.currentTimeMillis() + expire * 1000;
		v.value = value;
		map.put(key, v);
	}
	
	/**
	 * 添加一对key/value键值对, 使用全局缓存失效时间，默认为永不过期
	 * @param key 键
	 * @param value 值
	 */
	public void put(K key, V value) {
		put(key, value, defaultExpire);
	}
	
	/**
	 * 根据键获取该键对应的值
	 * @param key 键
	 * @return 不存在或者已经失效，返回null
	 */
	public V get(K key) {
		Value v = map.get(key);
		if(v != null) {
			if(v.expireTime == 0 || v.expireTime > System.currentTimeMillis())
				return v.value;
			else {
				map.remove(key);
			}
		}
		return null;
	}

	protected class Value {
		public V value;
		public long expireTime;
	}
}
