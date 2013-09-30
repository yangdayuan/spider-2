package com.netease.backend.collector.rss.crawler.config;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import java.io.File;

public class CrawlerConfig {
	private static final String SERVER_IP = "server-ip";
	
	private static final String SERVER_PORT = "server-port";
	
	private static final String REQUEST_URL_TIME = "request-url-time";
	
	private static final String TRIG_REQUEST_URL_LIMIT = "trig-request-url-limit";
	
	private static final String TRIG_REQUEST_URL_TIME = "trig-request-url-time";
	
	private static final String DATA_PATH = "data-path";
	
	private static final String MIRROR_PATH = "mirror-path";
	
	private static final String ORDER_PATH = "order-path";
	
	private static final String UPDATE_ARTICLE = "update-article";
	private static final String DUPLICFILTER_SERVER = "duplicFilter-server";
	
	private String serverIp;
	
	private short serverPort;
	
	private long requestUrlTime;
	
	private long trigRequestUrlLimit;
	
	private long trigRequestUrlTime;
	
	private String dataPath;
	
	private String mirrorPath;
	
	private String orderPath;
	
	private static CrawlerConfig instance = new CrawlerConfig();
	
	private CrawlerConfig() {}
	
	public static CrawlerConfig getInstance() {
		return instance;
	}
	
	public void init(String path) throws DricException {
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(path));
			Element root = document.getRootElement();
			
			serverIp = root.elementText(SERVER_IP);
			serverPort = Short.valueOf(root.elementText(SERVER_PORT));
			requestUrlTime = Long.valueOf(root.elementText(REQUEST_URL_TIME));
			trigRequestUrlLimit = Long.valueOf(root.elementText(TRIG_REQUEST_URL_LIMIT));
			trigRequestUrlTime = Long.valueOf(root.elementText(TRIG_REQUEST_URL_TIME));
			dataPath = root.elementText(DATA_PATH);
			mirrorPath = root.elementText(MIRROR_PATH);
			orderPath = root.elementText(ORDER_PATH);
		} catch (Exception e) {
			throw new DricException(e, ErrorCode.CRAWLER_CONF);
		}
	}

	public String getServerIp() {
		return serverIp;
	}

	public short getServerPort() {
		return serverPort;
	}

    /**
     * 如果从manager获取url失败，间隔多少时间尝试再获取，以毫秒为单位
     * @return 从manager获取url间隔
     */
	public long getRequestUrlTime() {
		return requestUrlTime;
	}

    /**
     * 到达多少url数量才能抓取
     * @return 一次的抓取数量
     */
	public long getTrigRequestUrlLimit() {
		return trigRequestUrlLimit;
	}

    /**
     * 达到多长时间才能抓取
     * @return 抓取间隔
     */
	public long getTrigRequestUrlTime() {
		return trigRequestUrlTime;
	}

	public String getDataPath() {
		return dataPath;
	}
	
	public String getMirrorPath() {
		return mirrorPath;
	}

	public String getOrderPath() {
		return orderPath;
	}

	public void setOrderPath(String orderPath) {
		this.orderPath = orderPath;
	}
	
}
