/**
 * 
 */
package com.netease.backend.collector.rss.common.client.service;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.collector.rss.common.client.proxy.CjlibProxyhandle;
import com.netease.backend.collector.rss.common.client.util.ClientUtil;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.net.DTException;
import com.netease.backend.collector.rss.common.net.TControlService;
import com.netease.backend.collector.rss.common.net.TControlService.Client;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.net.TUrlInfoResult;

/**
 * 中心节点客户端业务实现类
 * @author wangfuping
 * @since 2012-8-2
 */
public abstract class AbstractControlService  implements ControlService{
	
	public static final Logger logger = LoggerFactory.getLogger(AbstractControlService.class);
	
	private CjlibProxyhandle proxyhandle;
	
	/**
	 * 获取client客户端
	 * @return
	 */
	private Client getControlClient(){
		TControlService.Client client =  (Client) ClientUtil.getTServiceClient(TBinaryProtocol.class,TControlService.Client.class, proxyhandle.getSocket());
		return client;
	}

	public void setProxyHandle(CjlibProxyhandle proxyhandle) {
		this.proxyhandle = proxyhandle;
		
	}
	
	/**
	 * 请求待抓取链接
	 * @return
	 * @throws DricException
	 * @throws TException 
	 * @throws DTException 
	 */
    
    protected List<TURLInfo> requestURLProxy() throws  DTException, TException {
    	List<TURLInfo> urlInfoList = getControlClient().requestURL();
		return urlInfoList;
    }
    
    
    protected TUrlInfoResult sendURLProxy(List<TURLInfo> urlInfos) throws DTException, TException  {
    	TUrlInfoResult dupliList = getControlClient().sendURL(urlInfos);
		return dupliList;
    }
    
    
    protected boolean updateTimeProxy(String uuri, long modifyTime, long downLoadTime) throws DTException, TException {
    	boolean result = getControlClient().updateTime(uuri, modifyTime, downLoadTime);
		return result;
    }
	
	public void pingProxy(String server, int port) throws DTException, TException {
		getControlClient().ping(server, port);
		
	}

	
}
