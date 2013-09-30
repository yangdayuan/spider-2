package org.archive.crawler.frontier;

import org.apache.log4j.Logger;
import org.archive.crawler.framework.ToeThread;
import org.archive.crawler.framework.exceptions.EndedException;
import org.archive.util.ArchiveUtils;

import com.netease.backend.collector.rss.common.util.DricUtil;

public class PrisFrontier extends BdbFrontier {
	private static final Logger logger = Logger.getLogger(PrisFrontier.class);
	
	private transient long nextURIEmitTime = 0;
	
	public PrisFrontier(String name) {
		super(name);
	}

	private static final long serialVersionUID = ArchiveUtils.classnameBasedUID(PrisFrontier.class, 1);
	
	public void pause() {
		//DricUtil.printStackInfo();
		super.pause();
	}
	
	protected void decrementQueuedCount(long numberOfDeletes) {
		if (numberOfDeletes > 1) {
			logger.debug("numberOfDeletes = " + numberOfDeletes);
		}
		super.decrementQueuedCount(numberOfDeletes);
	}

	protected synchronized void preNext(long now) throws InterruptedException, EndedException {
		if (this.controller == null) {
			logger.error("controller is null");
		    return;
		}
		
		//logger.debug("preNext liveQueuedUriCount = " + queuedUriCount());
		
		// Check completion conditions
		if (this.controller.atFinish()) {
		    if (((Boolean)getUncheckedAttribute(null, ATTR_PAUSE_AT_FINISH)).booleanValue() && 
		    		!this.controller.getExitor().isExit()) {
		    	logger.debug("controller state is " + controller.getState());
		    	logger.debug("controller isEmpty = " + isEmpty());
		    	logger.debug("controller requestCrawlPause and pause-at-finish is true");
		        this.controller.requestCrawlPause();
		    } else {
		    	logger.debug("controller beginCrawlStop and pause-at-finish is false");
		        this.controller.beginCrawlStop();
	    		this.controller.completeExit();
		    }
		}
		
		// enforce operator pause
		if (shouldPause) {
		    while (shouldPause) {
		        this.controller.toePaused();
		        logger.debug("Thread should pause and begin wait");
		        wait();
		        logger.debug("Thread is notified");
		    }
		}
		
		// enforce operator terminate or thread retirement
		if (shouldTerminate
		        || ((ToeThread)Thread.currentThread()).shouldRetire()) {
			logger.error("shouldTerminate is " + shouldTerminate + " and will throw EndedException");
		    throw new EndedException("terminated");
		}
		
		enforceBandwidthThrottle(now);
	}
	
	 private void enforceBandwidthThrottle(long now) throws InterruptedException {
		 int maxBandwidthKB = ((Integer)getUncheckedAttribute(null, ATTR_MAX_OVERALL_BANDWIDTH_USAGE)).intValue();
		 if (maxBandwidthKB > 0) {
			 // Make sure that new bandwidth setting doesn't affect total crawl
			 if (maxBandwidthKB != lastMaxBandwidthKB) {
				 lastMaxBandwidthKB = maxBandwidthKB;
				 processedBytesAfterLastEmittedURI = totalProcessedBytes;
			 }
	
			 // Enforce bandwidth limit
			 long sleepTime = nextURIEmitTime - now;
			 float maxBandwidth = maxBandwidthKB * 1.024F; // Kilo_factor
			 long processedBytes = totalProcessedBytes - processedBytesAfterLastEmittedURI;
			 long shouldHaveEmittedDiff = nextURIEmitTime == 0? 0: nextURIEmitTime - now;
			 nextURIEmitTime = (long)(processedBytes / maxBandwidth) + now + shouldHaveEmittedDiff;
			 processedBytesAfterLastEmittedURI = totalProcessedBytes;
			 if (sleepTime > 0) {
				 long targetTime = now + sleepTime;
				 now = System.currentTimeMillis();
				 while (now < targetTime) {
					 synchronized (this) {
						 logger.debug("Frontier waits for: " + sleepTime + "ms to respect bandwidth limit.");
						 // TODO: now that this is a wait(), frontier can
						 // still schedule and finish items while waiting,
						 // which is good, but multiple threads could all
						 // wait for the same wakeTime, which somewhat
						 // spoils the throttle... should be fixed.
						 wait(targetTime - now);
					 }
					 now = System.currentTimeMillis();
				 }
			 }
		 }
	 }

}
