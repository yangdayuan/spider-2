package com.netease.backend.collector.rss.common.util;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;

public class DownloadUtil {
	public static byte[] downloadImage(String urlStr) throws Exception {
		InputStream is = null;
		try {
			URL url = new URL(urlStr);
			is = url.openStream();
			return IOUtils.toByteArray(is);
		} finally {
			if(is != null)
				try {
					is.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
		}
	}
}
