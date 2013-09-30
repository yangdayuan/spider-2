package com.netease.backend.collector.rss.common.util.sourceUrl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateRegexUrler extends RegexSourceUrler {
	public String makeRealUrl(String url) {
		if(url.length() < 6) {
			return null;
		}
		String regex = "\\[([^\\]]+)\\]";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(url);
		Date now = new Date(System.currentTimeMillis());
    	StringBuffer sb = new StringBuffer();
		int count = 0;
		while(matcher.find()) {
			String s = matcher.group(1).trim();
			DateFormat format = new SimpleDateFormat(s);
			matcher.appendReplacement(sb, format.format(now));
			count++;
			if(count > 10) {
				break;
			}
		}
    	matcher.appendTail(sb);
    	url = sb.toString();
    	return url;
	}
}
