/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统一些常用工具方法集
 * @author wuliufu
 */
public class CommonUtil {
	private static final Logger logger = LoggerFactory.getLogger(CommonUtil.class);
	private static HashMap<String,String> htmlEntities;
	/**
	 * 是否是测试服务进程
	 */
	private static boolean testServer = false;
	/**
	 * 位于pris.properties的所有配置值
	 */
	private static Properties pro = new Properties();
	/**
	 * 获取进程执行的classpath，以/结尾
	 */
	private static String classPath = null;
	static {
		URL url = CommonUtil.class.getResource("/");
		File file = FileUtils.toFile(url);
		classPath = file.getAbsolutePath().replaceAll("\\\\", "/") + "/";
		
		try {
			pro.load(new FileInputStream(getClassPathFile("pris.properties")));
			String ddbUrl = pro.getProperty("ddb.url");
			if(ddbUrl != null && ddbUrl.indexOf("_test") != -1) {
				testServer = true;
			}
		} catch (Exception e) {
			logger.error("", e);
		}
		
	}
	
	/**
	 * 获取进程执行的classpath，以/结尾
	 * @return
	 */
	public static String getClassPath() {
		return classPath;
	}
	
	/**
	 * 根据给定的name获取pris.properties里的配置值
	 * @param name
	 * @return
	 */
	public static String getProperty(String name) {
		return pro.getProperty(name);
	}
	
	public static String getFlashPalyerUrl() {
		return getProperty("flashPalyer.url");
	}
	
	/**
	 * 获取在claspath下的配置文件对象
	 * @param fileName 文件名
	 * @return
	 */
	public static File getClassPathFile(String fileName) {
		URL url = CommonUtil.class.getClassLoader().getResource(fileName);
		File file = FileUtils.toFile(url);
		return file;
	}
	
	/**
	 * 创建文件或目录
	 * @param parent 父目录
	 * @param fileName 文件或目录名
	 * @return 文件或目录对象
	 */
	public static File createFile(File parent, String fileName) {
		return new File(parent.getAbsolutePath() + File.separator + fileName);
	}
	
	public static boolean isTestServer() {
		return testServer;
	}
	
	public static final String unescapeHTML(String source) {
		if(source == null || source.length() == 0)
			return null;
		return unescapeHTML(source, 0);
	}
	
	 protected static final String unescapeHTML(String source, int start){
	     int i,j;

	     i = source.indexOf("&", start);
	     if (i > -1) {
	        j = source.indexOf(";" ,i);
	        if (j > i) {
	           String entityToLookFor = source.substring(i , j + 1);
	           String value = (String)htmlEntities.get(entityToLookFor);
	           if (value != null) {
	             source = new StringBuffer().append(source.substring(0 , i))
	                                   .append(value)
	                                   .append(source.substring(j + 1))
	                                   .toString();
	             return unescapeHTML(source, i + 1); // recursive call
	           }
	         }
	     }
	     return source;
	  }
	
	static {
	    htmlEntities = new HashMap<String,String>();
	    htmlEntities.put("&lt;","<")    ; htmlEntities.put("&gt;",">");
	    htmlEntities.put("&amp;","&")   ; htmlEntities.put("&quot;","\"");
	    htmlEntities.put("&agrave;","脿"); htmlEntities.put("&Agrave;","脌");
	    htmlEntities.put("&acirc;","芒") ; htmlEntities.put("&auml;","盲");
	    htmlEntities.put("&Auml;","脛")  ; htmlEntities.put("&Acirc;","脗");
	    htmlEntities.put("&aring;","氓") ; htmlEntities.put("&Aring;","脜");
	    htmlEntities.put("&aelig;","忙") ; htmlEntities.put("&AElig;","脝" );
	    htmlEntities.put("&ccedil;","莽"); htmlEntities.put("&Ccedil;","脟");
	    htmlEntities.put("&eacute;","茅"); htmlEntities.put("&Eacute;","脡" );
	    htmlEntities.put("&egrave;","猫"); htmlEntities.put("&Egrave;","脠");
	    htmlEntities.put("&ecirc;","锚") ; htmlEntities.put("&Ecirc;","脢");
	    htmlEntities.put("&euml;","毛")  ; htmlEntities.put("&Euml;","脣");
	    htmlEntities.put("&iuml;","茂")  ; htmlEntities.put("&Iuml;","脧");
	    htmlEntities.put("&ocirc;","么") ; htmlEntities.put("&Ocirc;","脭");
	    htmlEntities.put("&ouml;","枚")  ; htmlEntities.put("&Ouml;","脰");
	    htmlEntities.put("&oslash;","酶") ; htmlEntities.put("&Oslash;","脴");
	    htmlEntities.put("&szlig;","脽") ; htmlEntities.put("&ugrave;","霉");
	    htmlEntities.put("&Ugrave;","脵"); htmlEntities.put("&ucirc;","没");
	    htmlEntities.put("&Ucirc;","脹") ; htmlEntities.put("&uuml;","眉");
	    htmlEntities.put("&Uuml;","脺")  ; htmlEntities.put("&nbsp;"," ");
	    htmlEntities.put("&copy;","\u00a9");
	    htmlEntities.put("&reg;","\u00ae");
	    htmlEntities.put("&euro;","\u20a0");
	    htmlEntities.put("&#8226;","\u2022");
	  }
	
	/**
	 * 把MP4地址转换成带flash player和封面图的播放地址
	 * @param url MP4 url，要包含.mp4
	 * @param imageUrl 封面url
	 * @return  带flash player和封面图的播放地址
	 */
	public static String toFlashUrl(String url, String imageUrl) {
		if(StringUtils.isBlank(url)) {
			return url;
		}
		String termUrl = url.toLowerCase();
		if(termUrl.indexOf(".mp4") != -1 && termUrl.indexOf(".swf") == -1) {
			return CommonUtil.getFlashPalyerUrl() + "?videoUrl=" + url + (StringUtils.isBlank(imageUrl) ? "" : "&posterUrl=" + imageUrl);
		}
		return url;
	}
	
}
