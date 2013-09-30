package com.netease.backend.collector.rss.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.collector.rss.common.bdb.BdbManager;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.util.DricUtil;

/**
 * 待决策的url管理类
 * @author User
 *
 */
public class PendingURLManager {
	private static Logger logger = LoggerFactory.getLogger(PendingURLManager.class);
	public static final int DEFAULT_RETRYTIME = 30 * 60 * 1000;
	
	private static PendingURLManager instance = new PendingURLManager();
	
	private static final String PENDING_URLINFO_DB_NAME = "pendingURLInfo";
	private static final String PENDING_URLVIA_DB_NAME = "pendingURLVia";
	
	private static int pendingSize = 0;
	
	/**
	 * 是否初始化完成
	 */
	private boolean firstLoadTry = true;

    /** host -> Quene<urlitem> **/
	private Map<String, Queue<UrlItem>> hostPendingMap = Collections.synchronizedMap(new HashMap<String, Queue<UrlItem>>());
	
	private List<String> hostIter = new LinkedList<String>();
	private int hostIterCurr = 0;
	
	private PendingURLManager() {}
	
	public static PendingURLManager getInstance() {
		return instance;
	}
	
	public boolean init() throws DricException {
		BdbManager.getInstance().open(PENDING_URLINFO_DB_NAME, null, null);
		BdbManager.getInstance().open(PENDING_URLVIA_DB_NAME, null, null);
		//loadTryPendingUrl();
		return true;
	}
	
	public void unInit() {
		BdbManager.getInstance().close(PENDING_URLINFO_DB_NAME);
		BdbManager.getInstance().close(PENDING_URLVIA_DB_NAME);
	}
	
	public void loadTryPendingUrl() {		
		if (pendingSize != 0 && !firstLoadTry) {	
			return;
		}
		
		logger.info("Load try pending urls where firstLoadTry = " + firstLoadTry);
		
		firstLoadTry = false;
		
		//这里内存消耗可能比较大
		List<Object> values = BdbManager.getInstance().getAllValues(PENDING_URLINFO_DB_NAME);
		
		for (Object value: values) {
			URLInfo urlInfo = URLInfo.deserialize((byte[])value);
			if (BadURLManager.getInstance().needTryBadUrl(urlInfo) == false) {
				continue;
			}
			URLInfo assignedURLInfo = AssignURLManager.getInstance().getAssignedURLInfo(urlInfo.getUurl());
			if (assignedURLInfo != null) {
				long assignTime = assignedURLInfo.getAssignTime();
				long now = System.currentTimeMillis();
				long interval = now - assignTime;
				if (interval < urlInfo.getReuseInterval() || interval < DEFAULT_RETRYTIME) {
					logger.debug("url = " + urlInfo.getUurl() + 
							" reuseTime = " + urlInfo.getReuseInterval() + " continue");
					continue;
				}
				
				BadURLManager.getInstance().retry(urlInfo);
				byte[] data = urlInfo.serialize(); 
				//更新url的重试次数
				BdbManager.getInstance().put(PENDING_URLINFO_DB_NAME, urlInfo.getUurl(), data);
			}
			
			addPendingURL(urlInfo);
			addVia(urlInfo.getUurl(), urlInfo.getVia());
		}
		
		BdbManager.getInstance().sync(PENDING_URLINFO_DB_NAME);
		BdbManager.getInstance().sync(PENDING_URLVIA_DB_NAME);
	}

    /**
     * 添加urlinfo到等待队列
     * @param urlInfo urlinfo
     */
	private void addPendingURL(URLInfo urlInfo) {
		String host = DricUtil.getHost(urlInfo);
		synchronized(instance) {
			Queue<UrlItem> hostURLQueue = hostPendingMap.get(host);
			if (hostURLQueue == null) {
				hostURLQueue = new PriorityQueue<UrlItem>();
			}
			UrlItem item = new UrlItem(urlInfo.getTryTimes(), urlInfo.getUurl());
			hostURLQueue.add(item);
			hostPendingMap.put(host, hostURLQueue);
			pendingSize++;
		}
    }
	
	/**
	 * 测试一个uurl的via是否出现过
	 * @param uurl uurl
	 * @param via via
	 * @return 如果没有，则加入改uurl的viaSet中，返回true;如果出现过，返回false
	 */
	private boolean addVia(String uurl, String via) {
		byte[] data = (byte[])BdbManager.getInstance().get(PENDING_URLVIA_DB_NAME, uurl);
		HashSet<String> viaSet = DricUtil.BytesToViaSet(data);
		if (!viaSet.contains(via)) {
			viaSet.add(via);
			BdbManager.getInstance().put(PENDING_URLVIA_DB_NAME, uurl, DricUtil.ViaSetToBytes(viaSet));
			return true;
		} else {
			return false;
		}
	}

    /**
     * 添加urlinfos到等待队列
     * @param urlInfos urlinfos
     * @return 相同url不同via的urlInfo列表
     */
	public List<URLInfo> addPendingURL(List<URLInfo> urlInfos) {
		List<URLInfo> exitDiffViaUrlInfos = new LinkedList<URLInfo>();
		
		for (URLInfo urlInfo: urlInfos) {
			if (!BadURLManager.getInstance().needTryBadUrl(urlInfo)) {
				continue;
			}
			if (!BdbManager.getInstance().contain(PENDING_URLINFO_DB_NAME, urlInfo.getUurl())) {
				addPendingURL(urlInfo);
				byte[] data = urlInfo.serialize(); 
				BdbManager.getInstance().put(PENDING_URLINFO_DB_NAME, urlInfo.getUurl(), data);
				addVia(urlInfo.getUurl(), urlInfo.getVia());
				logger.info("add pending url= {} &via= {}.", urlInfo.getUurl(), urlInfo.getVia());
			} else {
				boolean isDupl = addVia(urlInfo.getUurl(), urlInfo.getVia());
				if (isDupl) {
					exitDiffViaUrlInfos.add(urlInfo);
					logger.debug("pending dupl url: " + urlInfo.getUurl());
					logger.debug("pending dupl via: " + urlInfo.getVia());
				}
			}
		}
		
		BdbManager.getInstance().sync(PENDING_URLINFO_DB_NAME);
		BdbManager.getInstance().sync(PENDING_URLVIA_DB_NAME);
		return exitDiffViaUrlInfos;
	}

    /**
     * 强制添加添加urlInfo到等待队列
     * @param urlInfos urlInfos
     * @return 无意义，永远返回true
     */
	public boolean addForcePendingURL(List<URLInfo> urlInfos) {
		boolean ret = true;
		
		for (URLInfo urlInfo: urlInfos) {
			if (!BadURLManager.getInstance().needTryBadUrl(urlInfo)) {
				continue;
			}
			addPendingURL(urlInfo);
			byte[] data = urlInfo.serialize(); 
			BdbManager.getInstance().put(PENDING_URLINFO_DB_NAME, urlInfo.getUurl(), data);
			addVia(urlInfo.getUurl(), urlInfo.getVia());
		}
		
		BdbManager.getInstance().sync(PENDING_URLINFO_DB_NAME);
		BdbManager.getInstance().sync(PENDING_URLVIA_DB_NAME);
		
		return ret;
	}
	
	public List<URLInfo> requestURLInfo(int size) {
		return requestURLInfo(size, false);
	}
	/**
	 * 请求待抓取的url
	 * @param size 希望请求url个数
	 * @param onlyNotTriedUrl 是否只取没有重试过的url，是设置为true，否则false
	 * @return 返回的已分配的urlinfo列表
	 */
	public List<URLInfo> requestURLInfo(int size, boolean onlyNotTriedUrl) {
		List<URLInfo> urlInfos = new LinkedList<URLInfo>();
		Set<String> noFirstTimeUrlSet = new HashSet<String>();
        logger.debug("==> Enter requestURLInfo &size= " + size + " &onlyNotTriedUrl= " + onlyNotTriedUrl);
		synchronized(instance) {
			while (urlInfos.size() < size && !hostPendingMap.isEmpty() && noFirstTimeUrlSet.size() < hostPendingMap.size()) {
                if (onlyNotTriedUrl)
                    logger.debug("==>Now noFirstTimeUrlSet.size= " + noFirstTimeUrlSet.size() + " &hostPendingMap.size= "
                            + hostPendingMap.size());

                if(hostIter.isEmpty()) {
                	hostIter.addAll(hostPendingMap.keySet());
                	hostIterCurr = 0;
                }
                
				while (hostIterCurr < hostIter.size()) {
					String host = hostIter.get(hostIterCurr++);
					Queue<UrlItem> hostURLQueue = hostPendingMap.get(host);
					if(hostURLQueue == null || hostURLQueue.isEmpty()) {
						continue;
					}
					UrlItem item = hostURLQueue.peek();
					if(onlyNotTriedUrl && item.getTryTimes() != 0) {
						if(!noFirstTimeUrlSet.contains(host)) {
							logger.debug("URI is not first time to assigned &url= " + item.getUrl() + " &trytimes= " + item.getTryTimes());
							noFirstTimeUrlSet.add(host);
						}
						continue;
					}
					item = hostURLQueue.poll();
					pendingSize--;
					byte[]data = (byte[])BdbManager.getInstance().get(PENDING_URLINFO_DB_NAME, item.getUrl());
					if (data != null) {
						URLInfo urlInfo = URLInfo.deserialize(data);
						urlInfos.add(urlInfo);
					} else {
                        logger.error("null urlInfo, url = " + item.getUrl());
					}
					if (hostURLQueue.isEmpty()) {
						hostPendingMap.remove(host);
					}
					if (urlInfos.size() == size) {
						return urlInfos;
					}
				}
				
				hostIter.clear();
			}
		}
        logger.debug("==> Leave requestURLInfo &size= " + size + " &onlyNotTriedUrl= " + onlyNotTriedUrl);
		return urlInfos;
	}

    /**
     * 将一个url移出等待队列，包括url对应的via集合
     * @param uurl url
     * @return url对应的via结合
     */
	public HashSet<String> removePendingDbURLInfo(String uurl) {
		synchronized(instance) {
			BdbManager.getInstance().remove(PENDING_URLINFO_DB_NAME, uurl);
			byte[]data = (byte[])BdbManager.getInstance().get(PENDING_URLVIA_DB_NAME, uurl);
			HashSet<String> viaSet = null /*new HashSet<String>()*/;
			if (data != null) {
				viaSet = DricUtil.BytesToViaSet(data);
				BdbManager.getInstance().remove(PENDING_URLVIA_DB_NAME, uurl);
				BdbManager.getInstance().sync(PENDING_URLINFO_DB_NAME);
			}
			return viaSet;
		}
	}
	
	 public static class UrlItem implements Comparable<UrlItem> {
			private int tryTimes;
			private String url;
			private long time;
			
			public UrlItem(int tryTimes, String url) {
				this.tryTimes = tryTimes;
				this.url = url;
				time = System.nanoTime();
			}
			public int getTryTimes() {
				return tryTimes;
			}
			public void setTryTimes(int tryTimes) {
				this.tryTimes = tryTimes;
			}
			public String getUrl() {
				return url;
			}
			public void setUrl(String url) {
				this.url = url;
			}
			public long getTime() {
				return time;
			}
			@Override
			public int compareTo(UrlItem o) {
				if(this.tryTimes == o.tryTimes) {
					return (int)(this.time - o.time);
				}
				return this.tryTimes - o.tryTimes;
			}		
	}
	
}
