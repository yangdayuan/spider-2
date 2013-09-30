package com.netease.backend.collector.rss.manager;

import org.apache.log4j.Logger;

import com.netease.backend.collector.rss.common.consts.Consts;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.task.Task;
import com.netease.backend.collector.rss.manager.config.ManagerConfig;

public class ManagerScheduler {
	private static Logger logger = Logger.getLogger(ManagerScheduler.class);

    /**
     * 决定一个url是否可以被重新抓取
     * @param urlInfo URLInfo对象
     * @return true 可以被重新抓取
     */
	public static boolean canReFetch(URLInfo urlInfo) {
		boolean ret = true;
		byte[] attach = urlInfo.getAttach();
		Task task = null;
		long downLoadTime = urlInfo.getDownLoadTime();
		long curTime = System.currentTimeMillis();
		if (curTime - urlInfo.getAssignTime() <= ManagerConfig.getInstance().getReassignInterval()) {
			return false;
		}
		
		try {
			task = Task.toTask(attach);
		} catch (DricException e) {
			logger.error("", e);
			return false;
		}

        // 已经被抓取过的图片url，不可以重新抓取
		if (task.getUrlType() == Consts.URL_IMAGE_TYPE && downLoadTime != 0) {
			return false;
		}
		
		if (urlInfo.getReuseInterval() == -1) {
			if (downLoadTime > 0) {
				return false;
			} else {
				return true;
			}
		} else if (urlInfo.getReuseInterval() == 0) {
			logger.warn("canReFetch url=" + urlInfo.getUurl() + "&& reuseInterval=" + urlInfo.getReuseInterval());
		}
		
		long timeDelta = curTime - downLoadTime;
		
		ret = timeDelta > urlInfo.getReuseInterval()? true: false;
		return ret;
	}
}
