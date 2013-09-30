package com.netease.backend.collector.rss.common.util.sourceUrl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class RegexSourceUrler {	
	private static Logger logger = Logger.getLogger(RegexSourceUrler.class);
	
	public static final int URL_CHANGE_TYPE_DATE = 1;
	
	public abstract String makeRealUrl(String url);
	
	public static String getRealUrl(String url, int type) {
		try {
			switch(type) {
			case URL_CHANGE_TYPE_DATE :
				RegexSourceUrler urler = new DateRegexUrler();
				String newurl = urler.makeRealUrl(url);
				if(newurl != null) {
					url = newurl;
				}
				break;
			default :
				break;
			}
		} catch(Throwable t) {		
			logger.error("", t);
		}
		return url;
	}
	
	public static void main(String[] args) {
		String url = "http://barb.sznews.com/html/[yyyy-MM/dd]/";
		System.out.println(RegexSourceUrler.getRealUrl(url, URL_CHANGE_TYPE_DATE));
		Date now = new Date(System.currentTimeMillis());
		DateFormat format = new SimpleDateFormat("yyyy-MM/dd");
		System.out.println(format.format(now));
	}
}
