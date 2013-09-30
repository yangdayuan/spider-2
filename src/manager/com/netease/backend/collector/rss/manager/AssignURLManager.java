package com.netease.backend.collector.rss.manager;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.util.DricUtil;
import com.netease.backend.collector.rss.common.bdb.BdbManager;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.List;

/**
 * 管理已经被分配出去的url，如果该url已经被抓取，更新modifyTime和downloadTime
 * @author User
 *
 */
public class AssignURLManager {
	private static final AssignURLManager instance = new AssignURLManager();
	
	private static final String ASSIGNED_URLINFO_DB_NAME = "assignedURLInfo";
	private static final String ASSIGNED_URLVIA_DB_NAME = "assignedURLVia";
	
	private static final Logger logger = Logger.getLogger(AssignURLManager.class);
	
	private AssignURLManager() {}
	
	public static AssignURLManager getInstance() {
		return instance;
	}
	
	public boolean init() throws DricException {
		logger.debug("Entered AssignURLManager init");
		BdbManager.getInstance().open(ASSIGNED_URLINFO_DB_NAME, null, null);
		BdbManager.getInstance().open(ASSIGNED_URLVIA_DB_NAME, null, null);
		logger.debug("Left AssignURLManager init");
		return true;
	}
	
	public void unInit() {
		logger.debug("Entered AssignURLManager unInit");
		BdbManager.getInstance().close(ASSIGNED_URLINFO_DB_NAME);
		BdbManager.getInstance().close(ASSIGNED_URLVIA_DB_NAME);
		logger.debug("Left AssignURLManager unInit");
	}
	
	/**
	 * 从数据库中获取urlInfo信息
	 * @param uurl URLInfo的标识符
	 * @return URLInfo
	 */
	public URLInfo getAssignedURLInfo(String uurl) {
		byte[] value = (byte[])BdbManager.getInstance().get(ASSIGNED_URLINFO_DB_NAME, uurl);
		if (value == null || value.length == 0) {
			return null;
		}
		
		return URLInfo.deserialize(value);
	}

    /**
     * 判断一个url是完全一样，还是url一样，但是via不同
     *
     * @param uurl url
     * @param via 经过的url
     * @return true 完全一样， false相同url不同via
     */
    public boolean isDuplicateUrl(String uurl, String via) {
        byte[] data = (byte[]) BdbManager.getInstance().get(ASSIGNED_URLVIA_DB_NAME, uurl);
        HashSet<String> viaSet = DricUtil.BytesToViaSet(data);
        return viaSet.contains(via);
    }

    /**
     * 添加via到这个url的via集合中
     * @param uurl url
     * @param via url via
     */
	public void addDuplicateURLVia(String uurl, String via) {
		byte[] data = (byte[])BdbManager.getInstance().get(ASSIGNED_URLVIA_DB_NAME, uurl);
		HashSet<String> viaSet = DricUtil.BytesToViaSet(data);
		viaSet.add(via);
		data = DricUtil.ViaSetToBytes(viaSet);
		if (data != null) {
			BdbManager.getInstance().put(ASSIGNED_URLVIA_DB_NAME, uurl, data);
			BdbManager.getInstance().sync(ASSIGNED_URLVIA_DB_NAME);
		}
	}
	
	public boolean putURLInfos(List<URLInfo> urlInfos) {
		for (URLInfo urlInfo: urlInfos) {
			BdbManager.getInstance().put(ASSIGNED_URLINFO_DB_NAME, urlInfo.getUurl(), urlInfo.serialize());
			this.addDuplicateURLVia(urlInfo.getUurl(), urlInfo.getVia());
		}
		
		if (urlInfos.size() > 0) {
			BdbManager.getInstance().sync(ASSIGNED_URLINFO_DB_NAME);
		}
		return true;
	}
	
	/**
	 * 根据url更改modifyTime和downLoadTime
	 * @param uurl URLInfo的标识符
	 * @param modifyTime 页面的modify时间
	 * @param downLoadTime 页面的download时间
	 * @return true 跟新成功
	 */
	public boolean updateTime(String uurl, long modifyTime, long downLoadTime) {
		//logger.debug("==> Entered URLManager updateTime");
		
		byte[] value = (byte[])BdbManager.getInstance().get(ASSIGNED_URLINFO_DB_NAME, uurl);
		if (value == null || value.length == 0) {
			return false;
		}
		
		URLInfo urlInfo = URLInfo.deserialize(value);
		urlInfo.setModifyTime(modifyTime);
		urlInfo.setDownLoadTime(downLoadTime);

		//logger.debug("updateTime url=" + uurl + "&&downLoadTime=" + urlInfo.getDownLoadTime());
		BdbManager.getInstance().put(ASSIGNED_URLINFO_DB_NAME, uurl, urlInfo.serialize());
		BdbManager.getInstance().sync(ASSIGNED_URLINFO_DB_NAME);
		//logger.debug("==> Left URLManager updateTime");
		return true;
	}
	
	public void setViaSet(String uurl, HashSet<String> viaSet) {
		if (viaSet != null && viaSet.size() != 0) {
			byte[] oriData = (byte[])BdbManager.getInstance().get(ASSIGNED_URLVIA_DB_NAME, uurl);
			HashSet<String> oriViaSet = DricUtil.BytesToViaSet(oriData);
			oriViaSet.addAll(viaSet);
			byte[] data = DricUtil.ViaSetToBytes(oriViaSet);
			BdbManager.getInstance().put(ASSIGNED_URLVIA_DB_NAME, uurl, data);
			BdbManager.getInstance().sync(ASSIGNED_URLVIA_DB_NAME);
		}
	}
}
