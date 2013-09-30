package org.archive.crawler.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import javax.management.AttributeNotFoundException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CoreAttributeConstants;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.framework.Processor;
import org.archive.crawler.settings.SimpleType;
import org.archive.io.RecordingInputStream;
import org.archive.io.ReplayInputStream;
import org.archive.net.UURI;
import org.archive.util.IoUtils;
import com.netease.backend.collector.rss.common.contentPage.ContentPage;
import com.netease.backend.collector.rss.common.contentPage.ContentPageFactory;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.task.Task;
import com.netease.backend.collector.rss.common.util.DateUtil;
import com.netease.backend.collector.rss.common.util.MD5Util;
import com.netease.backend.collector.rss.crawler.analyzer.AnalyzeHandlerFactory;
import com.netease.backend.collector.rss.crawler.analyzer.IAnalyzer;
import com.netease.backend.collector.rss.crawler.service.CrawlerService;

public class PageWriterProcessor extends Processor implements
		CoreAttributeConstants {
	private static final long serialVersionUID = 301407968828389168L;
	
	private static final Logger logger = Logger.getLogger(PageWriterProcessor.class);
	
	public static final String ATTR_PATH = "path";
	
	public static final String ATTR_SUFFIX = "default-suffix";
	
	public PageWriterProcessor(String name) {
		super(name, "write the page in directory and file name is random");
		
		addElementToDefinition(new SimpleType(ATTR_PATH,
	            "Top-level directory for mirror files.", "mirror"));
		addElementToDefinition(new SimpleType(ATTR_SUFFIX,
	            "Default suffix", "html"));
	}
	
	protected File writePage(CrawlURI curi) {
		UURI uuri = curi.getUURI();
		String baseDir = null; // Base directory.
        String baseSeg = null; // ATTR_PATH value.
        String defaultSuffix = null;
        try {
            baseSeg = (String) getAttribute(ATTR_PATH, curi);
            defaultSuffix = (String) getAttribute(ATTR_SUFFIX, curi);
        } catch (AttributeNotFoundException e) {
            logger.error(e.getLocalizedMessage());
            return null;
        }

        // Trim any trailing File.separatorChar characters from baseSeg.
        while ((baseSeg.length() > 1) && baseSeg.endsWith(File.separator)) {
            baseSeg = baseSeg.substring(0, baseSeg.length() - 1);
        }
        if (0 == baseSeg.length()) {
            baseDir = getController().getDisk().getPath();
        } else if ((new File(baseSeg)).isAbsolute()) {
            baseDir = baseSeg;
        } else {
            baseDir = getController().getDisk().getPath() + File.separator
                + baseSeg;
        }

        // Already have a path for this URI.
        boolean reCrawl = curi.containsKey(A_MIRROR_PATH);

        String mps = null;
        File destFile = null; // Write resource contents to this file.
        try {
            if (reCrawl) {
                mps = curi.getString(A_MIRROR_PATH);
            } else {
            	String urlPath = curi.getUURI().getPath();
            	String suffix = "";
            	String lastSeg = "";
            	int pos = urlPath.lastIndexOf("/");
            	if (pos == -1 || pos == urlPath.length()) {
            		suffix = defaultSuffix;
            	} else {
            		lastSeg = urlPath.substring(pos + 1);
            		pos = lastSeg.lastIndexOf(".");
    	            if (pos == -1 || pos == urlPath.length()) {
    	            	suffix = defaultSuffix;
    	            } else {
    	            	suffix = lastSeg.substring(pos + 1);
    	            }
            	}
            	
                String dayString = DateUtil.getDay(System.currentTimeMillis());
            	synchronized(this) {
                    File parent = new File(baseDir);
                    if (!parent.exists()) {
                        IoUtils.ensureWriteableDirectory(parent);
                    }
                	File dayDir = new File(baseDir + File.separator + dayString);
                	if(!dayDir.exists()) {
                		IoUtils.ensureWriteableDirectory(dayDir);
                	}
            	}
            	StringBuffer fileName = new StringBuffer();
            	fileName.append(dayString).append(File.separator).append(MD5Util.calcMD5(curi.getUURI().toString())).append("_")
            	.append(System.currentTimeMillis()).append(".").append(suffix);
                mps = fileName.toString();
            }
            
            destFile = new File(baseDir + File.separator + mps);

            if(logger.isDebugEnabled()) {
                logger.debug(uuri.toString() + " -> " + destFile.getPath());
            } else {
            	logger.info("Collect URL : " + uuri.toString() + " success.");
            }
            
            
            ReplayInputStream recis = curi.getHttpRecorder().getRecordedInput().getContentReplayInputStream();
            InputStream input = recis;
            HttpMethodBase method = (HttpMethodBase) curi.getObject(A_HTTP_TRANSACTION);
            Header endcoding = method.getResponseHeader("Content-Encoding");
            if(endcoding != null) {
            	String acceptEncoding = endcoding.getValue();
            	if(acceptEncoding != null && acceptEncoding.toLowerCase().indexOf("gzip") > -1) {
            		logger.debug("This page is gzip &acceptEncoding= " + acceptEncoding);
            		input = new GZIPInputStream(recis);
            	}
            }
            writeToPath(input, destFile);
            if (!reCrawl) {
                curi.putString(A_MIRROR_PATH, mps);
            }
            return destFile;
        } catch (IOException e) {
        	e.printStackTrace();
            curi.addLocalizedError(this.getName(), e, "Mirror");
            return null;
        }
	}
	
	private ContentPage getContentPage(CrawlURI curi, Task task, String filePath, String characterEncoding) {
		ContentPage contentPage = ContentPageFactory.newContentPage(task, filePath, characterEncoding, curi.getUURI().toString() );
		return contentPage;
	}
	
	private String getCharacterEncoding(CrawlURI curi) {
		String characterEncoding = curi.getHttpRecorder().getCharacterEncoding();
		String contentType = curi.getContentType();
		if(contentType == null ||characterEncoding == null){
			return characterEncoding == null ? "" : characterEncoding;
		}
		if (contentType.toLowerCase().indexOf(characterEncoding.toLowerCase()) == -1) {
			characterEncoding = "";
		}
		
		return characterEncoding;
	}
	
	private void updateTime(CrawlURI curi, HttpMethodBase method) {
		long modifyTime = 0;
    	Header header = method.getResponseHeader(A_LAST_MODIFIED_HEADER);
		if (header != null) {
			modifyTime = Date.parse(header.getValue());
		}
        CrawlerService.getInstance().updateTime(curi, modifyTime, curi.getLong(A_FETCH_COMPLETED_TIME));
	}
	
	protected void innerProcess(CrawlURI curi) {
        if (!curi.isSuccess()) {
        	logger.info(curi.getUURI().toString() + " is not success");
            return;
        }
        UURI uuri = curi.getUURI(); // Current URI.

        // Only http and https schemes are supported.
        String scheme = uuri.getScheme();
        if (!"http".equalsIgnoreCase(scheme)
                && !"https".equalsIgnoreCase(scheme)) {
        	logger.info(curi.getUURI().toString() + "&& Don't write the schema: " + scheme);
            return;
        }
        
        logger.debug("PageWriterProcessor curi:" + curi.getUURI().toString());
        
        HttpMethodBase method = null;
        if(!curi.containsKey(A_HTTP_TRANSACTION)) {
        	logger.error(curi.getUURI().toString() + " don't exist httpMethod");
        	return;
	    }
        method = (HttpMethodBase) curi.getObject(A_HTTP_TRANSACTION);

        RecordingInputStream recis = curi.getHttpRecorder().getRecordedInput();
        if (0L == recis.getResponseContentLength()) {
        	if (method.getResponseHeader("Location") != null) {
        		updateTime(curi, method);
        	}
        	logger.info(curi.getUURI().toString() + "&& responseContentLength is zero");
            return;
        }
        
        if (curi.getFetchStatus() == HttpStatus.SC_OK) {
	        
			try {
				File mirrorFile = writePage(curi);
				if (mirrorFile == null || !mirrorFile.exists()) {
					return;
				}
			     byte[] attach = (byte[])curi.getObject(URLInfo.ATTACH);
				 if (attach == null || attach.length == 0) return;
				 IAnalyzer analyzer = AnalyzeHandlerFactory.getAnalyzer(Task.toTask(attach).getUrlType());
				analyzer.analyze(curi, mirrorFile.getAbsolutePath(), getCharacterEncoding(curi));
			} catch (Exception e) {
				logger.error("", e);
				return;
			}
        } else if (method.getResponseHeader("Location") == null) {
        	logger.info("PageWriterProcessor: The http status of " + curi.getUURI().toString() + " is " + curi.getFetchStatus());
        }
		
		updateTime(curi, method);
    }
	
	/**
	    Copies a resource into a file.
	    A temporary file is created and then atomically renamed to
	    the destination file.
	    This prevents leaving a partial file in case of a crash.
	    of the resource
	    @param dest the destination file
	    @throws IOException on I/O error
	    @throws IOException if
	    the file rename fails
	 */
	 private void writeToPath(InputStream in, File dest)
	     throws IOException {
	     File tf = new File (dest.getPath() + "N");
	     FileOutputStream fos = new FileOutputStream(tf);
	     try {
	    	 IOUtils.copy(in, fos);
	     } finally {
	    	 if(fos != null) {
	    		 try {
	    			 fos.close();
	    		 } catch(IOException e) {	    			 
	    		 }
	    		 
	    	 }
	    	 
	    	 if(in != null) {
	    		 try {
	    			 in.close();
	    		 } catch(IOException e) {	    			 
	    		 }
	    		 
	    	 }
	     }
	     if (!tf.renameTo(dest)) {
	         throw new IOException("Can not rename " + tf.getAbsolutePath()
	                               + " to " + dest.getAbsolutePath());
	     }
	
	 }
}
