package com.netease.backend.collector.rss.manager;

import com.netease.backend.collector.rss.common.bdb.BdbManager;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.manager.bdb.UnfetcherUrlEntryTransformImpl;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class UnfetcherUrlManager {
	private static final Logger logger = Logger.getLogger(UnfetcherUrlManager.class);
	private static final String UNGETCHER_URLINFO_DB_NAME = "unfetcherURLInfo";
	private static UnfetcherUrlManager instance = new UnfetcherUrlManager();
	private UnfetcherUrlManager() {		
	}
	public static UnfetcherUrlManager getInstance() {
		return instance;
	}
	
	public boolean init() throws DricException {
		logger.debug("==> Entered UnfetcherUrlManager init");
		BdbManager.getInstance().open(UNGETCHER_URLINFO_DB_NAME, null, new UnfetcherUrlEntryTransformImpl());
		logger.debug("Left UnfetcherUrlManager init");
		return true;
	}
	
	/**
	 * 
	 * @param url 链接或者key
	 * @param expireTime 过期时间，单位秒
	 */
	private void addUrl(String url, int expireTime) {
		long exipe = -1;
		if(expireTime > 0)
			exipe = System.currentTimeMillis() + expireTime * 1000;
		BdbManager.getInstance().put(UNGETCHER_URLINFO_DB_NAME, url, exipe);
	}
	
	/**
	 * 把链接列表存储到bdb里面
	 * @param urls 链接列表
	 * @param expireTime 过期时间，单位秒
	 * @return 已经存在于数据库中的列表
	 */
	public List<String> addUrls(List<String> urls,  int expireTime) {
		boolean addUrl = false;
		List<String> existUrls = new LinkedList<String>();
		if(urls != null && urls.size() != 0) {
			for(String url : urls) {
				synchronized (instance) {
					try {
						Object val = BdbManager.getInstance().get(UNGETCHER_URLINFO_DB_NAME, url);
						if(val != null) {
							long value = (Long)val;
							if(value > System.currentTimeMillis() || value <= 0) {
								existUrls.add(url);
								continue;
							}
						}
						addUrl = true;
						addUrl(url, expireTime);
					} catch(Throwable t) {
						logger.error("", t);
					}

				}
			}
			if(addUrl) {
				BdbManager.getInstance().sync(UNGETCHER_URLINFO_DB_NAME);
			}
		}
		return existUrls;
	}
	
	/**
	 * 检查一个链接或key值是否可以重新使用，如果该值在bdb里不存在或者已经过期，那么返回true，否则返回false
	 * @param url
	 * @return
	 */
	public boolean canUseUrl(String url) {
		boolean result = true;
		synchronized (instance) {
			Object val = BdbManager.getInstance().get(UNGETCHER_URLINFO_DB_NAME, url);
			if(val != null) {
				long value = (Long)val;
				if(value > System.currentTimeMillis() || value <= 0) {
					BdbManager.getInstance().remove(UNGETCHER_URLINFO_DB_NAME, url);
					BdbManager.getInstance().sync(UNGETCHER_URLINFO_DB_NAME);
					result = false;
				}
			}
		}
		return result;
	}
	
	public void unInit() {
		logger.debug("==> Entered UnfetcherUrlManager unInit");
		BdbManager.getInstance().close(UNGETCHER_URLINFO_DB_NAME);
		logger.debug("Left UnfetcherUrlManager unInit");
	}
}
