package com.netease.backend.collector.rss.manager;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.util.DricUtil;
import com.netease.backend.collector.rss.common.bdb.BdbManager;
import com.netease.backend.collector.rss.manager.bdb.FailureHostCompare;
import com.netease.backend.collector.rss.manager.bdb.FailureHostEntryTransformImpl;
import com.netease.backend.collector.rss.manager.bdb.FailureUrlEntryTransformImpl;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class FailureURLManager {
	private static Logger logger = Logger.getLogger(FailureURLManager.class);
	
	private static FailureURLManager instance = new FailureURLManager();
	
	private static final String FAILURE_URLINFO_DB_NAME = "failureUrlInfo";
	
	private static final String FAILURE_HOST_DB_NAME = "failureHost";
	
	//private Set<String> hostSet = new HashSet<String>();
	
	private static final String HTTP = "http://";
	
	private FailureURLManager() {}
	
	public static FailureURLManager getInstance() {
		return instance;
	}
	
	public void init() throws DricException {
		logger.debug("==> Entered FailureURLManager init");
		BdbManager.getInstance().open(FAILURE_URLINFO_DB_NAME, null, new FailureUrlEntryTransformImpl());
		BdbManager.getInstance().open(FAILURE_HOST_DB_NAME, null, new FailureHostEntryTransformImpl());
		
		/*List<Object> hosts = BdbManager.getInstance().getAllValues(FAILURE_HOST_DB_NAME);
		for (Object host: hosts) {
			hostSet.add((String)host);
		}*/
		
		logger.debug("Left FailureURLManager init");
	}
	
	public void unInit() {
		logger.debug("==> Entered FailureURLManager unInit");
		
		BdbManager.getInstance().close(FAILURE_URLINFO_DB_NAME);
		BdbManager.getInstance().close(FAILURE_HOST_DB_NAME);
		
		logger.debug("Left FailureURLManager unInit");
	}

    /**
     * 添加抓取失败url，并将对应的host的次数+1
     * @param urlInfos urlinfos
     */
	public void addFailureURLInfos(List<URLInfo> urlInfos) {
		logger.debug("==> Entered FailureURLManager addFailureURLInfos");
		
		for (URLInfo urlInfo: urlInfos) {
			if (urlInfo == null) {
				continue;
			}
			
			byte[] data = urlInfo.serialize(); 
			BdbManager.getInstance().put(FAILURE_URLINFO_DB_NAME, urlInfo.getUurl(), data);
			
			String host = DricUtil.getHost(urlInfo);
			Long count = (Long)BdbManager.getInstance().get(FAILURE_HOST_DB_NAME, host);
			if (count == null) {
				count = 0L;
			}
			
			BdbManager.getInstance().put(FAILURE_HOST_DB_NAME, host, ++count);
			//hostSet.add(host);
		}
		
		BdbManager.getInstance().sync(FAILURE_URLINFO_DB_NAME);
		BdbManager.getInstance().sync(FAILURE_HOST_DB_NAME);
		
		logger.debug("Left FailureURLManager addFailureURLInfos");
	}
	
	public void removeFailureURLInfos(List<String> urls) {
		logger.debug("==> Entered FailureURLManager removeFailureURLInfos");
		
		for (String url: urls) {
			BdbManager.getInstance().remove(FAILURE_URLINFO_DB_NAME, url);
			
			String host = DricUtil.getHost(url);
			Long count = (Long)BdbManager.getInstance().get(FAILURE_HOST_DB_NAME, host);
			if (count == null || count == 0) {
				logger.error("failure host count don't exist or is zero && url = " + url + "&& count = " + count);
			} else {
				count--;
				if (count == 0) {
					BdbManager.getInstance().remove(FAILURE_HOST_DB_NAME, host);
					//hostSet.remove(host);
				} else {
					BdbManager.getInstance().put(FAILURE_HOST_DB_NAME, host, count);
				}
			}
		}
		
		BdbManager.getInstance().sync(FAILURE_URLINFO_DB_NAME);
		BdbManager.getInstance().sync(FAILURE_HOST_DB_NAME);
		
		logger.debug("Left FailureURLManager removeFailureURLInfos");
	}
	
	public List<String> getHosts() {
		logger.debug("==> Entered FailureURLManager getHosts");
		List<String> hostList = new LinkedList<String>();

		/*Object[] objs = hostSet.toArray();
		for (Object obj: objs) {
			if (obj == null) {
				continue;
			}
			hostList.add((String)obj);
		}*/
		
		List<Object> hosts = BdbManager.getInstance().getAllValues(FAILURE_HOST_DB_NAME);
		for (Object host: hosts) {
			hostList.add((String)host);
		}
		
		logger.debug("Left FailureURLManager getHosts");
		return hostList;
	}
	
	public List<URLInfo> getURLInfosByHost(String host) {
		logger.debug("==> Entered FailureURLManager getURLInfosByHost");
		if (!host.toLowerCase().startsWith(HTTP)) {
			host = HTTP + host;
		}
		
		List<URLInfo> urlInfos = new LinkedList<URLInfo>();
		
		URLInfo urlInfo = null;
		List<Object> objs = BdbManager.getInstance().getKeyRange(FAILURE_URLINFO_DB_NAME, host, new FailureHostCompare());
		for (Object obj: objs) {
			byte[] data = (byte[])obj;
			urlInfo = URLInfo.deserialize(data);
			if (urlInfo != null) {
				urlInfos.add(urlInfo);
			}
		}
		logger.debug("Left FailureURLManager getURLInfosByHost");
		
		return urlInfos;
	}
}
