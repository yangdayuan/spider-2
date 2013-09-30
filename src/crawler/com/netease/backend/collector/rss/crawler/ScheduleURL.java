package com.netease.backend.collector.rss.crawler;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.crawler.config.CrawlerConfig;
import com.netease.backend.collector.rss.crawler.service.CrawlerService;
import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.event.CrawlURIDispositionListener;
import org.archive.crawler.framework.CrawlController;

import java.util.List;

public class ScheduleURL implements CrawlURIDispositionListener {
	private static final Logger logger = Logger.getLogger(ScheduleURL.class);
	
	private Long lastRequestTime = 0l;
	
	private CrawlController controller = null;
	
	public ScheduleURL(CrawlController controller) {
		this.controller = controller;
	}

    // 请求并且安排url
	private int requestAndSchedule() {
		int count = 0;
		List<CandidateURI> caUris = null;
		logger.debug("==> Entered ScheduleURL requestAndSchedule && The state is " + controller.getState());
		
		try {
			caUris = CrawlerService.getInstance().requestURL();
        	if (caUris != null && caUris.size() > 0) {
        		for (CandidateURI caUri: caUris) {
        			controller.getFrontier().schedule(caUri);
        		}
        		count = caUris.size();
        	}
        	lastRequestTime = System.currentTimeMillis();
		} catch (DricException e) {
			logger.error("", e);
		}
		
		logger.debug("Left ScheduleURL requestAndSchedule && size = " + count);
		return count;
	}
	
	private void process(final CrawlURI curi) {
		logger.debug("ScheduleURL queuedUriCount = " + controller.getFrontier().queuedUriCount());
		long curTime = System.currentTimeMillis();
		int count = 0;
		
		/*
		 * 接收到进程退出后不再请求url
		 */
		if(controller.getExitor().isExit()) {
			return;
		}
		
		synchronized(this) {
			while (controller.getFrontier().queuedUriCount() == 0) {
				try {
		        	count = requestAndSchedule();
		        	if (count > 0) {
		        		logger.debug("在调用requestAndSchedule后，获得 " + count + " Url，等候队列中url数量： " + controller.getFrontier().queuedUriCount());
		        		break;
		        	}
		        	
		        	Thread.sleep(CrawlerConfig.getInstance().getRequestUrlTime());
				} catch (InterruptedException e) {
					logger.error("", e);
				} 
	        }
			
			if (controller.getFrontier().queuedUriCount() <= CrawlerConfig.getInstance().getTrigRequestUrlLimit()
					&& curTime - lastRequestTime >= CrawlerConfig.getInstance().getTrigRequestUrlTime()) {
				count = requestAndSchedule();
			}
		}
		
		if (count > 0) {
			logger.debug("Before requestCrawlResume, The state is " + controller.getState());
			controller.requestCrawlResume();
			logger.debug("After requestCrawlResume, The state is " + controller.getState());
		}
    }

	@Override
	public void crawledURIDisregard(CrawlURI curi) {
		logger.debug("crawledURIDisregard");
		process(curi);
	}

	@Override
	public void crawledURIFailure(CrawlURI curi) {
		logger.debug("crawledURIFailure");
		process(curi);
	}

	@Override
	public void crawledURINeedRetry(CrawlURI curi) {
		logger.debug("crawledURINeedRetry queuedUriCount = " + controller.getFrontier().queuedUriCount());
	}

	@Override
	public void crawledURISuccessful(CrawlURI curi) {
		logger.debug("crawledURISuccessful");
		process(curi);
	}
}
