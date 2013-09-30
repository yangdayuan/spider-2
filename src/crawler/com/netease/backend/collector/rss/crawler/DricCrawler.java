package com.netease.backend.collector.rss.crawler;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.framework.CrawlController;
import org.archive.crawler.settings.XMLSettingsHandler;

import com.netease.backend.collector.rss.common.client.ControlClient;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.crawler.config.CrawlerConfig;
import com.netease.backend.collector.rss.crawler.service.CrawlerService;

public class DricCrawler {
	private static final Logger logger = Logger.getLogger(DricCrawler.class);
	
	//Heritrix的控制器
    private CrawlController controller = null;
	
	public void init(String orderFile) throws DricException {
		logger.debug("==> Entered DricCrawler init");
		//order.xml文件
		File file = null;
        //读取order.xml文件的处理器
        XMLSettingsHandler handler = null;
        
        try {
            file = new File(orderFile);
            handler = new XMLSettingsHandler(file);
            //读取order.xml中的各个配置
            handler.initialize();
            
            controller = new CrawlController();
            //从读取的order.xml中的各个配置来初始化控制器
            controller.initialize(handler);
            controller.addCrawlURIDispositionListener(new ScheduleURL(controller));
        } catch (Exception e) {
            logger.error("", e);
            throw new DricException(e, ErrorCode.CRAWL_INIT);
        }
        logger.debug("Left DricCrawler init");
	}
	
	public void unInit() {
		if (controller != null) {
			controller.requestCrawlStop();
		}
	}
	
	public void schedule(CandidateURI caUri) {
		controller.getFrontier().schedule(caUri);
		/*if (!controller.isRunning()) {
			controller.requestCrawlResume();
		}*/
	}
	
	public void start() {
		logger.debug("==> Entered start crawler");
		controller.requestCrawlStart();//开始抓取
		logger.debug("Left start crawler");
	}
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("please set the config of crawler path and appName");
			return;
		}
		
		System.setProperty("logdir", "./ddblog-crawler");
		
		DricCrawler crawler = new DricCrawler();
		logger.debug("==> begin DricCrawler");
		
		try {
			CrawlerConfig.getInstance().init(args[0]);
			
			crawler.init(CrawlerConfig.getInstance().getOrderPath());
			CrawlURI.addAlistPersistentMember(URLInfo.ATTACH);
			
			String serverIp = CrawlerConfig.getInstance().getServerIp();
			short serverPort = CrawlerConfig.getInstance().getServerPort();
			ControlClient.getInstance().init(serverIp, serverPort);

			List<CandidateURI> caUris = null;
			
            while (true) {
            	caUris = CrawlerService.getInstance().requestURL();
            	if (caUris != null && caUris.size() > 0) {
            		for (CandidateURI caUri: caUris) {
            			crawler.schedule(caUri);
            		}
            		logger.debug("caUris size is " + caUris.size());
            		break;
            	}
            	
                Thread.sleep(CrawlerConfig.getInstance().getRequestUrlTime());
            }
            
            crawler.start();
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
