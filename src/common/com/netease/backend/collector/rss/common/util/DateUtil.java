package com.netease.backend.collector.rss.common.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {
	public static String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

	public static long string2Long(String time) throws Exception {
		StringBuilder sb = new StringBuilder(time);
		sb.deleteCharAt(time.lastIndexOf(':'));
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN);
		try {
			Date date = sdf.parse(sb.toString());
			return date.getTime();
		} catch (ParseException e) {
			throw new Exception(e);
		}
	}

    public static long string2Long(String time, String format) throws Exception {
        if (StringUtils.isBlank(time))
            return System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return sdf.parse(time).getTime();
        } catch (ParseException e) {
            throw new Exception("时间转换出错", e);
        }
    }

	public static String long2String(long time) {
		SimpleDateFormat format = new SimpleDateFormat(DATE_TIME_PATTERN);
		StringBuilder date = new StringBuilder(format.format(new Date(time)));
		date.insert(date.length() - 2, ':');
		return date.toString();
	}

	public static String getFullTime(long time) {
		String date = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
		int index = date.lastIndexOf("00:00:00");
		if(index > -1) {
			date = date.substring(0, index - 1);
		}
		return date;
	}

	public static Date getCST(String strGMT) throws ParseException {
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
				Locale.ENGLISH);
		return df.parse(strGMT);
	}

	public static long getCSTLong(String strGMT) throws ParseException {
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
				Locale.ENGLISH);
		Date d = df.parse(strGMT);
		return d.getTime();
	}

	public static String getGMT(long longCST) {
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
				Locale.ENGLISH);
		df.setTimeZone(TimeZone.getTimeZone("GMT")); // modify Time Zone.

		return (df.format(new Date(longCST)));
	}

	public static String getGMT(Date dateCST) {
		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
				Locale.ENGLISH);
		df.setTimeZone(TimeZone.getTimeZone("GMT")); // modify Time Zone.
		return (df.format(dateCST));
	}
	
	public static String getDay(long time) {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd",
				Locale.CHINA);
		return df.format(new Date(time));
	}

	public static void main(String[] args) throws Exception {
		string2Long("2010-11-26T18:30:02+08:00");
		String date = "2011-05-31 00:00:00";
		int index = date.lastIndexOf("00:00:00");
		if(index > -1) {
			date = date.substring(0, index - 1);
		}
		System.out.println(date);
	}
}
