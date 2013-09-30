package com.netease.backend.collector.rss.common.util;

import toolbox.lang.encdetect.EncodingGuess;
import toolbox.lang.encdetect.HtmlEncodingDetector;

public class IOUtil {
	/**
	 * 检测字节数组的编码
	 * @param bytes
	 * @return
	 */
	public static String detectEncoding(byte[] bytes) {
		String charset = null;
        EncodingGuess guess = HtmlEncodingDetector.detectEncoding(bytes, 0, bytes.length, 2048);
        if (guess.name != null && !guess.name.equalsIgnoreCase("OTHER")) {
        	charset = guess.name;
        }
        if(charset == null) {
        	charset = guess.declaredCharset;
        }
        if(charset == null) {
        	charset = "UTF-8";
        }
		return charset;
	}
}
