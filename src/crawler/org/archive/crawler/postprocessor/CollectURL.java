package org.archive.crawler.postprocessor;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.framework.Processor;

import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.crawler.service.CrawlerService;

public class CollectURL extends Processor implements FetchStatusCodes {
	private static final long serialVersionUID = -9138775477602250542L;
	
	private static final Logger logger = Logger.getLogger(CollectURL.class);
	 
	public CollectURL(String name) {
		super(name, "send link to center controller");
	}
	
	protected void innerProcess(final CrawlURI curi) {
		//logger.debug("CollectURL uuri = " + curi.getUURI() + "|| pathFromSeed = " + curi.getPathFromSeed() + "|| via = " + curi.getVia());
        // Handle any prerequisites when S_DEFERRED for prereqs
        if (curi.hasPrerequisiteUri() && curi.getFetchStatus() == S_DEFERRED) {
        	handlePrerequisites(curi);
            return;
        }

        List<CandidateURI> caUris = new LinkedList<CandidateURI>();
        synchronized(this) {
            for (CandidateURI caUri: curi.getOutCandidates()) {
            	if (caUri != null) {
            		String uuri = caUri.getUURI().toString();
            		if (uuri == null || uuri.length() == 0) {
            			logger.error("send url is null or empty");
            		}
            		caUri.putObject(URLInfo.ATTACH, curi.getObject(uuri));
            		caUris.add(caUri);
            	}
            }
        }
        
        if (caUris.size() > 0) {
        	CrawlerService.getInstance().sendURL(caUris);
        }
    }
	
	protected void handlePrerequisites(CrawlURI curi) {
        schedule((CandidateURI)curi.getPrerequisiteUri());
    }
	
	protected void schedule(CandidateURI caUri) {
        getController().getFrontier().schedule(caUri);
    }
}
