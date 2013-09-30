package com.netease.backend.collector.rss.manager.service;

import java.util.List;

import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.manager.URLManager;
import com.netease.backend.collector.rss.manager.meta.UrlInfoResult;

public class ManagerService {
	private static final ManagerService instance = new ManagerService();
	
	private URLManager urlManager;
	
	private ManagerService() {}
	
	public static ManagerService getInstance() {
		return instance;
	}
	
	public void init(URLManager urlManager) {
		this.urlManager = urlManager;
	}
	
	public List<URLInfo> requestURL() {
		return urlManager.requestURLInfo();
	}

	public UrlInfoResult sendURL(List<URLInfo> urlInfos) {
		return urlManager.addPendingURL(urlInfos);
	}
	
	public boolean updateTime(String uuri, long modifyTime, long downLoadTime) {
		boolean ret = true;
		//logger.debug("==> Entered ManagerService updateTime && url = " + uuri);
		ret = urlManager.updateTime(uuri, modifyTime, downLoadTime);
		//logger.debug("==> Left ManagerService updateTime");
		return ret;
	}
}
