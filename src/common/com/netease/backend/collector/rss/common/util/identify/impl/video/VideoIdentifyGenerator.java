/**
 * 
 */
package com.netease.backend.collector.rss.common.util.identify.impl.video;

import org.apache.commons.lang.StringUtils;

import com.netease.backend.collector.rss.common.util.identify.impl.IdentifyGenerator;

/**
 * @author wuliufu
 */
public class VideoIdentifyGenerator extends IdentifyGenerator<String> {
	public static VideoIdentifyGenerator instance = new VideoIdentifyGenerator();

	@Override
	public String generate(String originalUrl) {
		return Long.toString(createKey(StringUtils.trimToEmpty(originalUrl)));
	}
}
