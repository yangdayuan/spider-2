/**
 * 
 */
package com.netease.backend.collector.rss.crawler.exit;

import org.archive.crawler.framework.CrawlController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exit.ExitObservable;
import com.netease.backend.collector.rss.common.exit.ExitObserver;

/**
 * @author wuliufu
 *
 */
public class CrawlerExitor extends ExitObserver {
	private static final long serialVersionUID = 7576321496934669447L;
	private static final Logger logger = LoggerFactory.getLogger(CrawlerExitor.class);
	private boolean exit = false;
	private CrawlController controller;

	/* (non-Javadoc)
	 * @see com.netease.backend.collector.rss.common.exit.IExitObserver#notifyExit(com.netease.backend.collector.rss.common.exit.ExitObservable, java.lang.Object)
	 */
	@Override
	public void notifyExit(ExitObservable o, Object args) throws DricException {
		super.notifyExit(o, args);
		if(controller != null) {
			logger.debug("Queued uri count is {}.", controller.getFrontier().queuedUriCount());
		}
	}

	public boolean isExit() {
		return exit;
	}

	public CrawlController getController() {
		return controller;
	}

	public void setController(CrawlController controller) {
		this.controller = controller;
	}

}
