package com.netease.backend.collector.rss.manager.config;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.netease.backend.collector.rss.common.consts.Consts;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;

/**
 * manager节点配置文件数据管理类
 * @author wuliufu
 */
public class ManagerConfig {
	/**
	 * 该节点监听ip或服务器域名
	 */
	private static final String SERVER_IP = "server-ip";
	
	/**
	 * 该节点监听端口
	 */
	private static final String SERVER_PORT = "server-port";
	
	/**
	 * BDB缓存百分比
	 */
	private static final String BDB_CACHE_PERCENT = "bdb-cache-percent";
	
	/**
	 * url去重状态路径，state目录
	 */
	private static final String STATE_DB_PATH = "state-db-path";
	
	/**
	 * 每次最小抓取url数
	 */
	private static final String FETCH_URL_LIMIT = "fetch-url-limit";
	
	/**
	 * url最多重试分发次数
	 */
	private static final String URL_RETRIES = "url-retries";
	
	/**
	 * 源URL重新使用时间间隔
	 */
	private static final String SEED_REUSE_INTERVAL = "seed-reuse-interval";
	
	/**
	 * URL重新使用时间间隔
	 */
	private static final String GENERAL_REUSE_INTERVAL = "general-reuse-interval";
	
	/**
	 * 重新加载源链接时间间隔
	 */
	private static final String RELOAD_URL_INTERVAL = "reload-url-interval";
	
	/**
	 * 重新分配时间间隔
	 */
	private static final String REASSIGN_INTERVAL = "reassign-interval";
	
	/**
	 * 加载源所使用的sql,只供测试情况下使用
	 */
	private static final String LOAD_SOURCE_SQL = "load-source-sql";
	
	/**
	 * 抓取优先级，每个源都归属到一套抓取系统里去抓取
	 */
	private static final String FETCH_PRIORITY = "fetch-priority";
	
	/**
	 * 该组系统是否对文章做更新操作
	 */
	private static final String UPDATE_ARTICLE = "update-article";
	
	private static final long DEFAULT_SEED_REUSE_INTERVAL = 86400000; //默认为一天
	
	private static final long DEFAULT_GENERAL_REUSE_INTERVAL = 604800000; //默认为七天
		
	private static final long DEFAULT_RELOAD_URL_INTERVAL = 600000; //默认为10分钟
	
	private static final long DEFAULT_REASSIGN_INTERVAL = 3600000; //默认为1小时
	
	private String serverIp;
	
	private short serverPort;
	
	private int bdbCachePercent;
	
	private String stateDbPath;
	
	private int fetchUrlLimit;
	
	private int urlRetries;
	
	private String loadSourceSql = null;
	
	private int fetchPriority = Consts.FETCH_PRIORITY_ALL;
	
	private long seedReuseInterval = DEFAULT_SEED_REUSE_INTERVAL;
	
	private long generalReuseInterval = DEFAULT_GENERAL_REUSE_INTERVAL;
	
	private long reloadUrlInterval = DEFAULT_RELOAD_URL_INTERVAL;
	
	private long reassignInterval = DEFAULT_REASSIGN_INTERVAL;
	
	//单位毫秒
	private int reloadWeiboInterval = 10 * 60 * 1000;
	
	private int requestWeiboLimit = 50;
	
	private boolean updateArticle = false;
	
	private List<Object[]> duplicFilterServers = new LinkedList<Object[]>();
	
	private static ManagerConfig instance = new ManagerConfig();
	
	private ManagerConfig() {}
	
	public static ManagerConfig getInstance() {
		return instance;
	}
	
	public void init(String path) throws DricException {
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read(new File(path));
			Element root = document.getRootElement();
			
			serverIp = root.elementText(SERVER_IP);
			serverPort = Short.valueOf(root.elementText(SERVER_PORT));
			bdbCachePercent = Integer.valueOf(root.elementText(BDB_CACHE_PERCENT));
			stateDbPath = root.elementText(STATE_DB_PATH);
			fetchUrlLimit = Integer.valueOf(root.elementText(FETCH_URL_LIMIT));
			urlRetries = Integer.valueOf(root.elementText(URL_RETRIES));
			seedReuseInterval = Long.valueOf(root.elementText(SEED_REUSE_INTERVAL));
			generalReuseInterval = Long.valueOf(root.elementText(GENERAL_REUSE_INTERVAL));
			reloadUrlInterval = Long.valueOf(root.elementText(RELOAD_URL_INTERVAL));
			reassignInterval = Long.valueOf(root.elementText(REASSIGN_INTERVAL));
			String loadSourceSqlStr = root.elementText(LOAD_SOURCE_SQL);
			if(StringUtils.isNotBlank(loadSourceSqlStr)) {
				loadSourceSql = loadSourceSqlStr;
			}
			String fetchPriorityStr = getElementText(root.element(FETCH_PRIORITY));
			if(fetchPriorityStr != null) {
				fetchPriority = Integer.parseInt(fetchPriorityStr);
			}
			
			reloadWeiboInterval = getInt(root, "reload-weibo-interval");
			requestWeiboLimit = getInt(root, "request-weibo-limit");
			if(requestWeiboLimit == 0) {
				requestWeiboLimit = 50;
			}
			
			Element updateArticleElem = root.element(UPDATE_ARTICLE);
			if(updateArticleElem != null) {
				updateArticle = Boolean.parseBoolean(updateArticleElem.getStringValue());
			}
		} catch (Exception e) {
			throw new DricException(e, ErrorCode.MANAGER_CONF);
		}
	}
	
	public int getReloadWeiboInterval() {
		return reloadWeiboInterval;
	}

	public String get(Element e, String name) {
		return e.elementText(name);
	}
	
	public int getInt(Element e, String name) {
		String str = e.elementText(name);
		if(str != null) {
			return Integer.parseInt(str);
		}
		return 0;
	}
	
	public String getElementText(Element e) {
		if(e == null)
			return null;
		return e.getStringValue();
	}

	public int getBdbCachePercent() {
		return bdbCachePercent;
	}

	public String getStateDbPath() {
		return stateDbPath;
	}

	public int getFetchUrlLimit() {
		return fetchUrlLimit;
	}

    /**
     * 获取一个url的最大尝试次数
     * @return url的尝试次数
     */
	public int getUrlRetries() {
		return urlRetries;
	}

	public String getServerIp() {
		return serverIp;
	}

	public short getServerPort() {
		return serverPort;
	}

	public long getSeedReuseInterval() {
		return seedReuseInterval;
	}

	public long getGeneralReuseInterval() {
		return generalReuseInterval;
	}

	public long getReloadUrlInterval() {
		return reloadUrlInterval;
	}

    /**
     * url重新分配间隔
     * @return url重新分配间隔时间
     */
	public long getReassignInterval() {
		return reassignInterval;
	}

	public String getLoadSourceSql() {
		return loadSourceSql;
	}

	public int getFetchPriority() {
		return fetchPriority;
	}

	public int getRequestWeiboLimit() {
		return requestWeiboLimit;
	}

	public boolean isUpdateArticle() {
		return updateArticle;
	}

	public List<Object[]> getDuplicFilterServers() {
		return duplicFilterServers;
	}
}
