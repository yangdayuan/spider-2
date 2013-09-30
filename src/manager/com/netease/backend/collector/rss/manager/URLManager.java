package com.netease.backend.collector.rss.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.task.TMallTask;
import com.netease.backend.collector.rss.common.task.Task;
import com.netease.backend.collector.rss.common.util.DateUtil;
import com.netease.backend.collector.rss.common.util.ValidateUtil;
import com.netease.backend.collector.rss.manager.config.ManagerConfig;
import com.netease.backend.collector.rss.manager.meta.UrlInfoResult;

/**
 * 中心节点的url管理类
 * @author XinDingfeng
 *
 */
public class URLManager extends TimerTask {
	private static Logger logger = LoggerFactory.getLogger(URLManager.class);

	private List<URLInfo> pendingSeeds = Collections.synchronizedList(new LinkedList<URLInfo>());
	
	private Set<String> pendingSeedSet = Collections.synchronizedSet(new HashSet<String>());
	
	private List<URLInfo> pendingSpecialUrls = Collections.synchronizedList(new LinkedList<URLInfo>());
	
	private Set<String> pendingSpecialUrlSet = Collections.synchronizedSet(new HashSet<String>());
	
	/**
	 * urlmanager初始化
	 * @return
	 * @throws DricException
	 */
	public boolean init() throws DricException {
		boolean ret = true;
		logger.debug("==> Entered URLManager init");

		loadSeed();
		//fetchSpecialUrl();
		
		AssignURLManager.getInstance().init();
		BadURLManager.getInstance().init();
		
		PendingURLManager.getInstance().init();
		FailureURLManager.getInstance().init();
		UnfetcherUrlManager.getInstance().init();
		
		logger.debug("==> Left URLManager init");
		return ret;
	}
	
	protected void fetchSpecialUrl() throws DricException {
		
	}
	
	/**
	 * 加载种子url信息
	 * @throws DricException
	 */
	private void loadSeed() throws DricException {
		
		//TODO 怎样定时从哪里加载种子?
		String url = "http://detail.tmall.com/item.htm?id=17523538425";
		URLInfo urlInfo = AssignURLManager.getInstance().getAssignedURLInfo(url);
		
		if (urlInfo == null) {
			urlInfo = new URLInfo();
		}
		urlInfo.setUurl(url);
		urlInfo.setSeed(true);
		urlInfo.setReuseInterval(60 *1000);
		Task task = new TMallTask(url);
		urlInfo.setAttach(task.toBytes());
		pendingSeeds.add(urlInfo);
		pendingSeedSet.add(urlInfo.getUurl());
		logger.info("Reload size of source url is : {}", pendingSeeds.size());
	}
	
	/**
	 * 获取需要抓取的url列表
     * 使用策略，75%抓没抓过的，另外25%用于其他分配，包括special url和普通源
	 * @return 分配的url
	 */
	public List<URLInfo> requestURLInfo() {
		List<URLInfo> urlInfos = new LinkedList<URLInfo>();
		int i = 0;
		URLInfo urlInfo = null;
		long assignTime = System.currentTimeMillis();
		int fetchUrlLimit = ManagerConfig.getInstance().getFetchUrlLimit();
		
		logger.debug("==> Entered URLManager requestURLInfo");
		
		//优先加载没有分配过的url
		int maxSize = (int)(fetchUrlLimit * 0.75);
		urlInfos.addAll(PendingURLManager.getInstance().requestURLInfo(maxSize, true));
		int firstSize = urlInfos.size();
		logger.info("Request first time to assign size is " +  urlInfos.size());
		
		for (i = 0; i < fetchUrlLimit - firstSize; i++) {
			if (pendingSpecialUrls.size() > 0) {
				urlInfo = pendingSpecialUrls.remove(0);
				pendingSpecialUrlSet.remove(urlInfo.getUurl());
			} else if (pendingSeeds.size() > 0) {
				urlInfo = pendingSeeds.remove(0);
				pendingSeedSet.remove(urlInfo.getUurl());
			} else {
				break;
			}
			
			if (urlInfo != null) {
				urlInfos.add(urlInfo);
			}			
		}
		logger.info("Request source's size is " +  (urlInfos.size() - firstSize));
		
		int delta = fetchUrlLimit - urlInfos.size();
		if (delta > 0) {
			urlInfos.addAll(PendingURLManager.getInstance().requestURLInfo(delta));
		}
		
		for (i = 0; i < urlInfos.size(); i++) {
			urlInfo = urlInfos.get(i);
			urlInfo.setAssignTime(assignTime);
			
			//修改log
			if(urlInfo.isSeed()) {
				logger.info("RequestSourceUrl url= " + urlInfo.getUurl() + 
						" &downLoadTime= " + DateUtil.getFullTime(urlInfo.getDownLoadTime()));
			} else {
				logger.info("RequestURL url=" + urlInfo.getUurl() + " &tryTimes=" + urlInfo.getTryTimes() + " &&via= " + urlInfo.getVia() + 
						" &&downLoadTime= " + DateUtil.getFullTime(urlInfo.getDownLoadTime()));
			}

		}
		
		AssignURLManager.getInstance().putURLInfos(urlInfos);
		
		logger.info("==> Left URLManager requestURLInfo && size = " + urlInfos.size());
		return urlInfos;
	}
	
	
	/**
	 * 添加需要被抓取的url列表
	 * @param urlInfos urlinfos
	 * @return 这次不在抓取任务中，但存在相同的url，不同的via的URLInfo列表
	 */
	public UrlInfoResult addPendingURL(List<URLInfo> urlInfos) {
		//logger.debug("==> Entered URLManager addPendingURL");
		List<URLInfo> exitDiffViaUrlInfos = new LinkedList<URLInfo>();      // 不同via,相同url
		List<URLInfo> pendingURLInfos = new LinkedList<URLInfo>();
		for (URLInfo urlInfo: urlInfos) {
            // 判断url是否合法
            if (!ValidateUtil.isLegalUrl(urlInfo.getUurl())) {
            	logger.info("Illegal url : " + urlInfo.getUurl());
            	continue;
            }
                
			URLInfo value = AssignURLManager.getInstance().getAssignedURLInfo(urlInfo.getUurl());
			if (value != null) {
				urlInfo.setModifyTime(value.getModifyTime());
				urlInfo.setDownLoadTime(value.getDownLoadTime());
				urlInfo.setAssignTime(value.getAssignTime());
				//urlInfo.setReuseInterval(value.getReuseInterval());
			}
			
			if (urlInfo.getReuseInterval() == 0) {
				long reuseInterval = urlInfo.isSeed()? ManagerConfig.getInstance().getSeedReuseInterval(): 
					ManagerConfig.getInstance().getGeneralReuseInterval();
				urlInfo.setReuseInterval(reuseInterval);
			}

            // 添加相同url不同via的url到viaset中
			if (!ManagerScheduler.canReFetch(urlInfo)) {    // 不能重新抓取
				if (!AssignURLManager.getInstance().isDuplicateUrl(urlInfo.getUurl(), urlInfo.getVia())) {
					exitDiffViaUrlInfos.add(urlInfo);
					logger.info("duplicate url= " + urlInfo.getUurl() + " &viaUrl= " + urlInfo.getVia());
					AssignURLManager.getInstance().addDuplicateURLVia(urlInfo.getUurl(), urlInfo.getVia());
				}
				continue;
			}
			
			pendingURLInfos.add(urlInfo);
			//logger.debug(urlInfo.getUurl() + "&& attach size: " + urlInfo.getAttach().length);
		}
		
		exitDiffViaUrlInfos.addAll(PendingURLManager.getInstance().addPendingURL(pendingURLInfos));

		//logger.debug("==> Left URLManager addPendingURL");
		return new UrlInfoResult(exitDiffViaUrlInfos, pendingURLInfos);
	}
	
	public boolean addForceFetchURLInfos(List<URLInfo> urlInfos) {
		return PendingURLManager.getInstance().addForcePendingURL(urlInfos);
	}

    /**
     * 更新url的下载时间和修改时间，同时更新对应的viaset
     * @param uurl uurl
     * @param modifyTime 修改时间
     * @param downLoadTime 下载时间
     * @return true 更新成功
     */
	public boolean updateTime(String uurl, long modifyTime, long downLoadTime) {
		boolean ret = true;
		if (AssignURLManager.getInstance().updateTime(uurl, modifyTime, downLoadTime)) {
			HashSet<String> viaSet = PendingURLManager.getInstance().removePendingDbURLInfo(uurl);
			AssignURLManager.getInstance().setViaSet(uurl, viaSet);
		} else {
			ret = false;
		}
		return ret;
	}
	
	/**
	 * 关闭urlmanager
	 */
	public void unInit() {
		logger.debug("==> Entered URLManager unInit");
		UnfetcherUrlManager.getInstance().unInit();
		FailureURLManager.getInstance().unInit();
		PendingURLManager.getInstance().unInit();
		BadURLManager.getInstance().unInit();
		AssignURLManager.getInstance().unInit();
		logger.debug("==> Left URLManager unInit");
	}

	@Override
	public void run() {
		logger.debug("==> Entered URLManager run");

		try {
			loadSeed();
			//fetchSpecialUrl();
			LoadTryPendingUrlThread t = new LoadTryPendingUrlThread();
			t.setDaemon(true);
			t.start();
//			PendingURLManager.getInstance().loadTryPendingUrl();
		} catch (Throwable e) {
			logger.error("", e);
		}
		
		logger.debug("==> Left URLManager run");
	}
	
	private static class LoadTryPendingUrlThread extends Thread {
		private transient static boolean working = false;
		public void run() {
			if(working) {
				return;
			}
			try {
				working = true;
				PendingURLManager.getInstance().loadTryPendingUrl();
			} finally {
				working = false;
			}
		}
	}
}
