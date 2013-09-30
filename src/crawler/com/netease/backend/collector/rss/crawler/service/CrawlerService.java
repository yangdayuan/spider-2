package com.netease.backend.collector.rss.crawler.service;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CandidateURI;

import com.netease.backend.collector.rss.common.client.ControlClient;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.net.TUrlInfoResult;
import com.netease.backend.collector.rss.common.util.DricUtil;

public class CrawlerService {
	private static final CrawlerService instance = new CrawlerService();
	
	private static final Logger logger = Logger.getLogger(CrawlerService.class);
	
	private CrawlerService() {}
	
	public static CrawlerService getInstance() {
		return instance;
	}

    /**
     * 请求url，并将url转化为候选url
     * @return CandidateURL列表
     * @throws DricException DricException
     */
	public List<CandidateURI> requestURL() throws DricException {
		//logger.debug("==> Entered CrawlerService requestURL");
		List<CandidateURI> caUris = new LinkedList<CandidateURI>();
		try {
			List<TURLInfo> turlInfos = ControlClient.getInstance().requestURL();
			for (TURLInfo turlInfo: turlInfos) {
				URLInfo urlInfo = DricUtil.TURLInfo2URLInfo(turlInfo);
				if (urlInfo == null) {
					continue;
				}
				
				CandidateURI caUri = DricUtil.URLInfo2CandidateURI(urlInfo);
				
				if (caUri != null) {
					//logger.debug("requestURL: " + caUri.getUURI() + "|| pathFromSeed = " + caUri.getPathFromSeed() + "|| via = " + caUri.getVia());
					caUris.add(caUri);
				}
			}
		} catch (DricException e) {
			logger.error("", e);
		}
		//logger.debug("==> Left CrawlerService requestURL && size = " + caUris.size());
		return caUris;
	}

    /**
     * 将获取到的新的url发送给manager
     * @param caUris CandidateURI列表
     * @return 相同url不同via的url列表
     */
	public List<URLInfo> sendURL(List<CandidateURI> caUris) {
		List<TURLInfo> turlInfos = new LinkedList<TURLInfo>();
		List<URLInfo> urlInfos = new LinkedList<URLInfo>();
		
		//logger.debug("==> Entered CrawlerService sendURL");
		
		for (CandidateURI caUri: caUris) {
			URLInfo urlInfo = DricUtil.CandidateURI2URLInfo(caUri);
			if (urlInfo == null) {
				continue;
			}
			
			TURLInfo turlInfo = DricUtil.URLInfo2TURLInfo(urlInfo);
			if (turlInfo == null) {
				continue;
			}
			
			turlInfos.add(turlInfo);
			
			//logger.debug("sendURL " + urlInfo.getUurl());
		}
		
		try {
			TUrlInfoResult urlInfoResult = ControlClient.getInstance().sendURL(turlInfos);
			for (TURLInfo exitDiffViaTUrlInfo: urlInfoResult.getExitDiffViaUrlInfos()) {
				urlInfos.add(DricUtil.TURLInfo2URLInfo(exitDiffViaTUrlInfo));
			}
		} catch (DricException e) {
			logger.error("", e);
		}
		
		//logger.debug("==> Left CrawlerService sendURL");
		
		return urlInfos;
	}

    /**
     * 更新url的修改时间和下载时间
     * @param caUri url
     * @param modifyTime 修改时间
     * @param downLoadTime 下载时间
     * @return true 更新成功
     */
	public boolean updateTime(CandidateURI caUri, long modifyTime, long downLoadTime) {
		boolean ret = true;
		//logger.debug("==> Entered CrawlerService updateTime && uri: " + caUri.toString());
		try {
			ret = ControlClient.getInstance().updateTime(caUri.getUURI().toString(), modifyTime, downLoadTime);
		} catch (DricException e) {
			logger.error("", e);
			ret = false;
		}
		//logger.debug("==> Left CrawlerService updateTime");
		return ret;
	}
}
