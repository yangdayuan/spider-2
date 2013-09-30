/**
 * 
 */
package com.netease.backend.collector.rss.common.util;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
/**
 * @author wuliufu
 *
 */
public class ValidateUtil {
	private static final Pattern URL_PATTERN = Pattern.compile("^(http|https|ftp)://[^<>\"'\r\n]+$", Pattern.CASE_INSENSITIVE);
	/**
	 * 验证url合法性
	 * @param url
	 * @return
	 */
	public static boolean isLegalUrl(String url) {
		if(StringUtils.isBlank(url)) {
			return false;
		}
		
		url = url.trim();
		if(url.length() > 1024) {
			return false;
		}
		
		Matcher m = URL_PATTERN.matcher(url);
		return m.matches();
	}
}
