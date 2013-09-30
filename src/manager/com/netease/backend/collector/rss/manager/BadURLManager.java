package com.netease.backend.collector.rss.manager;

import org.apache.log4j.Logger;

import com.netease.backend.collector.rss.common.bdb.BdbManager;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.task.ContentType;
import com.netease.backend.collector.rss.common.task.Task;
import com.netease.backend.collector.rss.manager.bdb.BadUrlEntryTransformImpl;
import com.netease.backend.collector.rss.manager.config.ManagerConfig;

/**
 * 管理不能一次被抓取的url尝试次数
 * @author User
 *
 */
public class BadURLManager {
	private static BadURLManager instance = new BadURLManager();
	
	/**
	 * 该数据库存储的 key / value 为[String / Integer],即 url / 重试次数
	 */
	private static final String BAD_URLINFO_DB_NAME = "badURLInfo";
	
	private static final Logger logger = Logger.getLogger(BadURLManager.class);
	
	private BadURLManager() {}
	
	public static BadURLManager getInstance() {
		return instance;
	}
	
	public boolean init() throws DricException {
		logger.debug("==> Entered BadURLManager init");
		BdbManager.getInstance().open(BAD_URLINFO_DB_NAME, null, new BadUrlEntryTransformImpl());
		logger.debug("Left BadURLManager init");
		return true;
	}

	/**
	 * 调用前应该先调用needTryBadUrl判断是否需要重试
	 * @param urlInfo
	 */
	public void retry(URLInfo urlInfo) {
		Integer tryTimes = (Integer)BdbManager.getInstance().get(BAD_URLINFO_DB_NAME, urlInfo.getUurl());
		if (tryTimes == null) {
			tryTimes = 1;
		} else {
			tryTimes++;
		} 
		//设置重试次数的值
		urlInfo.setTryTimes(tryTimes);
		BdbManager.getInstance().put(BAD_URLINFO_DB_NAME, urlInfo.getUurl(), tryTimes);
		BdbManager.getInstance().sync(BAD_URLINFO_DB_NAME);
	}
	
	/**
	 * 判断该urlInfo是否需要重试,超过最大重试次数的url将被移出等待队列
     *
	 * @param urlInfo urlinfo
	 * @return false 不需要重试
	 */
	public boolean needTryBadUrl(URLInfo urlInfo) {
		Integer tryTimes = (Integer)BdbManager.getInstance().get(BAD_URLINFO_DB_NAME, urlInfo.getUurl());
		if (tryTimes == null) {     // 没抓过，当然要重试
			return true;
		}
		
		if (tryTimes >= ManagerConfig.getInstance().getUrlRetries()) {
			PendingURLManager.getInstance().removePendingDbURLInfo(urlInfo.getUurl());
			Task task = null;
			try {
				task = Task.toTask(urlInfo.getAttach());
			} catch (DricException e) {
				logger.error("", e);
			}
			
			if (task != null && task.getContentType() == ContentType.IMAGE) {
				//TODO 图片下载失败重试无效后怎样处理？
			}
			
            logger.info("The tries of " + urlInfo.getUurl() + " is " + tryTimes +
            					" and must remove from pending db");
			
			return false;
		} else {
			logger.info("The tries of " + urlInfo.getUurl() + " is " + tryTimes);
			return true;
		}
	}
	
	public void unInit() {
		logger.debug("==> Entered BadURLManager unInit");
		BdbManager.getInstance().close(BAD_URLINFO_DB_NAME);
		logger.debug("Left BadURLManager unInit");
	}
}
