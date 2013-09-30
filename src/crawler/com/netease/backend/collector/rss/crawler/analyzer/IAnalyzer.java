/**
 * 
 */
package com.netease.backend.collector.rss.crawler.analyzer;

import org.archive.crawler.datamodel.CrawlURI;

/**
 * @author wuliufu
 *
 */
public interface IAnalyzer {
	
	boolean analyze(CrawlURI curi, final String filePath, final String characterEncoding);
}
