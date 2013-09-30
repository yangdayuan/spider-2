package com.netease.backend.collector.rss.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netease.security.xssdefender.filter.XSSFilter;

/**
 * Xss工具类,防止xss攻击
 * @author wfp
 *
 */
public class XssUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(XssUtil.class);
	
	private static long FILTER_TIME_OUT = 20 * 1000;
	
	private static XSSFilter filter;
	
	private static XSSFilter getFilter() throws Exception{
	  if(filter == null){
		 filter = new XSSFilter();
	  }
	  return filter; 
	}
	
	/**
	 * 过滤普通的HTML，这个是默认类型
	 * @param html
	 * @return
	 */
	 public  static String filterNormalHtml(String content) {
		try {
			return getFilter().getFilteredHTML(content, XSSFilter.TYPE_NORMAL_HTML, FILTER_TIME_OUT);
		} catch (Throwable t) {
            logger.error("", t);
		}
		return content;
	}
	
	/**
	 * 过滤属性url
	 * @param content  过滤内容作为一个url 属性填充到页面中，如img 标签的src地址，a 标签的href 链接等
	 * @return
	 */
	 public static String filterAttributeURL(String content) {
		try {
			return getFilter().getFilteredHTML(content, XSSFilter.TYPE_ATTRIBUTE_URL, FILTER_TIME_OUT);
		} catch (Throwable t) {
            logger.error("", t);
		}
		return content;
	}
	
	/**
	 * 过滤属性样式
	 * @param content  过滤内容内容作为标签的样式填充到页面中
	 * @return
	 */
	 public static String filterAttributeStyle(String content) {
		try {
			return getFilter().getFilteredHTML(content, XSSFilter.TYPE_ATTRIBUTE_STYLE, FILTER_TIME_OUT);
		} catch (Throwable t) {
            logger.error("", t);
		}
		return content;
	}
	
	/**
	 * 过滤纯文本
	 * @param content 过滤内容内容作为纯文本信息显填充到页面中
	 * @return
	 */
	 public static String filterPlainText(String content) {
		try {
			return getFilter().getFilteredHTML(content, XSSFilter.TYPE_PLAIN_TEXT, FILTER_TIME_OUT);
		} catch (Throwable t) {
            logger.error("", t);
		}
		return content;
	}
	
    /**
     * 过滤其他属性值
     * @param content 过滤内容为其他属性值
     * @return
     */
	 public static String filterAttributeOther(String content) {
		try {
			return getFilter().getFilteredHTML(content, XSSFilter.TYPE_ATTRIBUTE_OTHER, FILTER_TIME_OUT);
		} catch (Throwable t) {
            logger.error("", t);
		}
		return content;
	}

}
