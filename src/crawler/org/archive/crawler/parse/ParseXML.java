package org.archive.crawler.parse;

import java.util.List;

import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.Processor;
import org.archive.io.RecordingInputStream;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.task.Task;

public class ParseXML extends Processor implements CoreAttributeConstants, FetchStatusCodes {
	private static final long serialVersionUID = 5852375241080471017L;
	
	private static final Logger logger = Logger.getLogger(ParseXML.class);
	
	public ParseXML(String name) {
		super(name, "parse xml in order to extractor link");
	}
	
	public void innerProcess(CrawlURI curi) {
		if (!curi.isSeed()) {
			return;
		}
		
		byte[] attach = null;
		if (curi.containsKey(URLInfo.ATTACH)) {
			attach = (byte[])curi.getObject(URLInfo.ATTACH);
		}
		
		if (attach == null || attach.length == 0) {
			curi.setFetchStatus(S_BLOCKED_BY_ATTACH);
            curi.skipToProcessorChain(getController().getPostprocessorChain());
            return;
		}
		
		RecordingInputStream recis = curi.getHttpRecorder().getRecordedInput();
        if (0L == recis.getResponseContentLength()) {
        	curi.setFetchStatus(S_BLOCKED_BY_NOSTREAM);
            curi.skipToProcessorChain(getController().getPostprocessorChain());
            return;
        }
		
        try {
			Task task = Task.toTask(attach);
			List<Task> outTasks = null;
			for (Task outTask: outTasks) {
				curi.createAndAddLink(outTask.getUrl(), Link.SPECULATIVE_MISC, Link.SPECULATIVE_HOP);
				UURI outUURI = UURIFactory.getInstance(curi.getUURI(), outTask.getUrl());
				curi.putObject(outUURI.toString(), outTask.toBytes());
				//logger.debug(outUURI.toString() + "&& attach size = " + outTask.toBytes().length);
			}
        } catch (Exception e) {
			logger.error("", e);
			//curi.setFetchStatus(S_BLOCKED_BY_PARSE);
            curi.skipToProcessorChain(getController().getPostprocessorChain());
		}
	}
}
