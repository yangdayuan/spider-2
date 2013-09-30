package com.netease.backend.collector.rss.common.client;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.pool.ObjectPool;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.collector.rss.common.client.provider.imp.SynProxyConnectionProvider;
import com.netease.backend.collector.rss.common.client.proxy.CjlibProxyhandle;
import com.netease.backend.collector.rss.common.client.service.ControlService;
import com.netease.backend.collector.rss.common.client.service.impl.ControlServiceImpl;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.net.DTException;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.net.TUrlInfoResult;

/**
 * 连接中心结点的client
 * @author hzwfp@corp.netease.com
 * @version 1.0
 *
 */
public class ControlClient {
	
	public static final Logger logger = LoggerFactory.getLogger(ControlClient.class);
	
	//默认的socketTimeout时间
	private  static final  int socketTimeOut =  10*60*1000;
	
	//连接池的最小连接对象数量
	private  static final  int DEFAULT_MIN_IDLE =  1;
	
	//连接池的最大连接对象数量
	private static final  int DEFAULT_MAX_IDLE =  256;
	
	private CjlibProxyhandle proxyhandle;

	private ControlService controlService;
	
	private static ControlClient instance = new ControlClient();

	
	public static ControlClient getInstance() {
		return instance;
	}

	/**
	 * 通信客户端启动函数
	 * @param serverIp
	 * @param port
	 */
    public void init(String serverIp, int port){
    	synchronized (this) {
    		String hosts = serverIp + ":"+port;
    	   	String[] hostsList = hosts.split(",");
    		setControlService(hostsList, socketTimeOut , DEFAULT_MIN_IDLE ,DEFAULT_MAX_IDLE);
		}
    }
    
    
    /**
     * 关闭通信客户端
     */
    public void unInit(){
    	if(proxyhandle == null ||proxyhandle.getConnectionProvider() == null){
    		return;
    	}
    	List<ObjectPool> objectPools = proxyhandle.getConnectionProvider().getObjectPools();
    	if(objectPools == null || objectPools.size() <=0){
    		return;
    	}
    	try {
    		for(ObjectPool objectPool : objectPools){
    			objectPool.close();
    		}
		} catch (Exception e) {
			logger.error("close socket pool occured exception",e.getCause());
		}
    }
    
    
    private void setControlService(String[] hostsList , int timeOut ,int minIdle , int maxIdle){
    	SynProxyConnectionProvider connectionProvider =  new SynProxyConnectionProvider(hostsList,timeOut,minIdle,maxIdle);
    	connectionProvider.setRetryConstantly(true);
		proxyhandle = new CjlibProxyhandle(connectionProvider);
		controlService = (ControlService) proxyhandle.getProxy(ControlServiceImpl.class);
		controlService.setProxyHandle(proxyhandle);
    }
    
    /**
     * 获取最大空闲连接数
     * @param hostNum host数量
     * @return 可设置的最大连接数
     */
    @SuppressWarnings("unused")
	private int getMaxIdle(int hostNum){
    	int maxIdle = 0;
    	int cpuSize = Runtime.getRuntime().availableProcessors();
    	if(cpuSize <= hostNum ){
    		maxIdle = 1;
    	}else if( cpuSize > hostNum){
    		double value = (double) cpuSize/hostNum;
    		maxIdle = getRound(value);
    	}
    	if(maxIdle * hostNum < DEFAULT_MAX_IDLE){
    		double value = (double)  DEFAULT_MAX_IDLE / hostNum;
    		maxIdle = getRound(value);
		}
    	return maxIdle;
    }
    
    
    /**
     * 获取最小空闲连接数
     * @param hostNum
     * @return
     */
    @SuppressWarnings("unused")
	private int getMinIdle(int hostNum){
    	int minIdle = 0;
    	int cpuSize = Runtime.getRuntime().availableProcessors();
    	if(cpuSize <= DEFAULT_MIN_IDLE ){
    		minIdle = 1;
    	}
    	if((minIdle * hostNum) < DEFAULT_MIN_IDLE){
    		double value = (double)  DEFAULT_MIN_IDLE / hostNum;
    		minIdle = getRound(value);
		}
    	return minIdle;
    }
    
    /**
     * 获取四舍五入后的数
     * @param value
     * @return 四舍五入值
     */
    private int getRound(double value){
    	BigDecimal bigDecimal = new BigDecimal(value);
        int roundValue = bigDecimal.setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
        return roundValue;
    }

	public CjlibProxyhandle getProxyhandle() {
		return proxyhandle;
	}

	/**
	 * 请求待抓取链接
	 * @return
	 * @throws DricException
	 */
	public List<TURLInfo> requestURL() throws DricException{
		return controlService.requestURL();
	}

	/**
	 * 发送需要抓取的链接
	 * @param urlInfos
	 * @return
	 * @throws DricException
	 */
	public TUrlInfoResult sendURL(List<TURLInfo> urlInfos) throws DricException{
		return controlService.sendURL(urlInfos);
	}
	
	/**
	 * 更新链接资源的下载时间
	 * @param uuri
	 * @param modifyTime
	 * @param downLoadTime
	 * @return
	 * @throws DricException
	 */
	public boolean updateTime(String uuri, long modifyTime, long downLoadTime) throws DricException{
		return controlService.updateTime(uuri, modifyTime, downLoadTime);
	}

	public void ping(String server, int port) throws DTException, TException{
		 controlService.ping(server, port);
	}

}
