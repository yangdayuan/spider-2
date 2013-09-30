/**
 * 
 */
package com.netease.backend.collector.rss.common.task;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author wuliufu
 */
public enum ContentType {
	/**
	 * text/html/xhtml/xml
	 */
	TEXT_HTML_XHTML(0),
	/**
	 * 图片
	 */
	IMAGE(1);
	
	private static Map<Integer, ContentType> map = new TreeMap<Integer, ContentType>();
	private int type;

	static {
		Class<?> c = ContentType.class;
		Object[] objs = c.getEnumConstants();
		for (Object obj : objs) {
			ContentType ct = (ContentType)obj;
			map.put(ct.getType(), ct);
		}
	}
	
	public static ContentType getContentType(int type) {
		ContentType contentType = map.get(type);
		if(contentType == null) {
			contentType = TEXT_HTML_XHTML;
		}
		return contentType;
	}
	
	private ContentType(int type) {
		this.type = type;
	}

	/**
	 * 获取type
	 * @return type type
	 */
	public int getType() {
		return type;
	}
	
}
