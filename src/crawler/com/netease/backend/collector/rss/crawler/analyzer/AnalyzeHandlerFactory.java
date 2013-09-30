package com.netease.backend.collector.rss.crawler.analyzer;

import com.netease.backend.collector.rss.common.consts.Consts;
import com.netease.backend.collector.rss.crawler.analyzer.impl.TMallAnalyzer;

/**
 * 分析器选择工程
 * @author wfp
 *
 */
public class AnalyzeHandlerFactory {
	
	/**
	 * 获取分析器
	 * @param urlType url类型;淘宝单品、商城单品、蘑菇街
	 * @return
	 */
	public static IAnalyzer getAnalyzer(int urlType) {
		IAnalyzer analyzer = null;
		switch (urlType) {
		case Consts.URL_TMALL_PAGE_TYPE:
			analyzer = new TMallAnalyzer();
		default:
			break;
		}
		return analyzer;
	}
}
