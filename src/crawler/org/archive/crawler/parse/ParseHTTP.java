package org.archive.crawler.parse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.Processor;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;

import com.netease.backend.collector.rss.common.meta.URLInfo;

public class ParseHTTP extends Processor implements CoreAttributeConstants {
	 private static final long serialVersionUID = 8499072198555554647L;

	    private static final Logger logger = Logger.getLogger(ParseHTTP.class.getName());
	    protected long numberOfCURIsHandled = 0;
	    protected long numberOfLinksExtracted = 0;

	    public ParseHTTP(String name) {
	        super(name,
	            "HTTP parse. Parse URIs from HTTP response headers.");
	    }

	    public void innerProcess(CrawlURI curi) {
	        if (!curi.isHttpTransaction() || curi.getFetchStatus() <= 0) {
	            // If not http or if an error status code, skip.
	            return;
	        }
	        numberOfCURIsHandled++;
	        HttpMethod method = (HttpMethod)curi.getObject(A_HTTP_TRANSACTION);
	        if (method.getResponseHeader("Location") != null) {
	        	addHeaderLink(curi, method.getResponseHeader("Location"));
	        	
	        	curi.setNextProcessor(curi.nextProcessorChain().getFirstProcessor());
                curi.setNextProcessorChain(curi.nextProcessorChain().getNextProcessorChain());
	        }
	        
	        addHeaderLink(curi, method.getResponseHeader("Content-Location"));
	    }

	    protected void addHeaderLink(CrawlURI curi, Header loc) {
	        if (loc == null) {
	            // If null, return without adding anything.
	            return;
	        }
	        // TODO: consider possibility of multiple headers
	        try {
	        	/**
	        	 * 302重定向使用自定义的方法存储link
	        	 * @modify: wuliufu
	        	 * @since : 2012-05-11
	        	 */
	            curi.createAndAddLocationLink(curi.getVia(), loc.getValue(), loc.getName() + ":",
	                Link.REFER_HOP);
	            
	            if (curi.getObject(URLInfo.ATTACH) != null) {
	            	UURI outUURI = UURIFactory.getInstance(curi.getUURI(), loc.getValue());
	            	logger.debug("ParseHTTP: curi = " + curi.getUURI().toString() + "&& " + loc.getName() + "=" + outUURI.toString());
	            	curi.putObject(outUURI.toString(), curi.getObject(URLInfo.ATTACH));
	            }
	            
	            numberOfLinksExtracted++;
	        } catch (URIException e) {
	            // There may not be a controller (e.g. If we're being run
	            // by the extractor tool).
	            if (getController() != null) {
	                getController().logUriError(e, curi.getUURI(), loc.getValue());
	            } else {
	            	logger.info(curi + ", " + loc.getValue() + ": " +
	                    e.getMessage());
	            }
	        }

	    }

	    public String report() {
	        StringBuffer ret = new StringBuffer();
	        ret.append("Processor: org.archive.crawler.extractor.ExtractorHTTP\n");
	        ret.append("  Function:          " +
	            "Extracts URIs from HTTP response headers\n");
	        ret.append("  CrawlURIs handled: " + numberOfCURIsHandled + "\n");
	        ret.append("  Links extracted:   " + numberOfLinksExtracted + "\n\n");
	        return ret.toString();
	    }
}
