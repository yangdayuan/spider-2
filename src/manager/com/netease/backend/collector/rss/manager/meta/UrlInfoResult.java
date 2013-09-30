/**
 * 
 */
package com.netease.backend.collector.rss.manager.meta;

import java.util.List;

import com.netease.backend.collector.rss.common.meta.URLInfo;

/**
 * @author wuliufu
 *
 */
public class UrlInfoResult {
	  private List<URLInfo> exitDiffViaUrlInfos; // required
	  private List<URLInfo> pendingURLInfos;
	public UrlInfoResult(List<URLInfo> exitDiffViaUrlInfos,
			List<URLInfo> pendingURLInfos) {
		this.exitDiffViaUrlInfos = exitDiffViaUrlInfos;
		this.pendingURLInfos = pendingURLInfos;
	}
	public List<URLInfo> getExitDiffViaUrlInfos() {
		return exitDiffViaUrlInfos;
	}
	public List<URLInfo> getPendingURLInfos() {
		return pendingURLInfos;
	}
	  
}
