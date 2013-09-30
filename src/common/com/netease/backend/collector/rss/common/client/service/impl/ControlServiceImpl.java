/**
 * 
 */
package com.netease.backend.collector.rss.common.client.service.impl;
import java.util.List;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netease.backend.collector.rss.common.client.service.AbstractControlService;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.common.net.DTException;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.net.TUrlInfoResult;

/**
 * 中心节点客户端业务实现类
 * @author wangfuping
 * @since 2012-8-2
 */
public class ControlServiceImpl extends AbstractControlService{
	
	public static final Logger logger = LoggerFactory.getLogger(ControlServiceImpl.class);
	
	
	/**
	 * 请求待抓取链接
	 * @return
	 * @throws DricException
	 */
    public  List<TURLInfo> requestURL() throws DricException {
    	return requestURL(false);
    }
    
    private List<TURLInfo> requestURL(boolean retry) throws DricException {
    	List<TURLInfo> urlInfoList = null;
		try {
			urlInfoList = requestURLProxy();
		} catch (DTException e) {
			throw new DricException(e, e.getErrorCode());
		} catch (TException e) {
			logger.error("", e);
			if(!retry){
				
				urlInfoList = requestURL(true);
			}else{
				throw new DricException(e, ErrorCode.T_TRANSPORT_ERROR);
			}
		}
		return urlInfoList;
    }
    
    /**
     * 发送需要抓取的链接
     * @param urlInfos
     * @return
     * @throws DricException
     */
    public  TUrlInfoResult sendURL(List<TURLInfo> urlInfos) throws DricException {
    	return sendURL(urlInfos, false);
    }
    
    private TUrlInfoResult sendURL(List<TURLInfo> urlInfos, boolean retry) throws DricException {
    	TUrlInfoResult dupliList;
		try {
			dupliList = sendURLProxy(urlInfos);
		} catch (DTException e) {
			throw new DricException(e, e.getErrorCode());
		} catch (TException e) {
			if(!retry){
				dupliList = sendURL(urlInfos, true);
			}else{
				throw new DricException(e, ErrorCode.T_TRANSPORT_ERROR);
			}
		}
		return dupliList;
    }

    /**
     * 更新链接资源的下载时间
     * @param uuri
     * @param modifyTime
     * @param downLoadTime
     * @return
     * @throws DricException 
     */
    public  boolean updateTime(String uuri, long modifyTime, long downLoadTime) throws DricException {
    	return updateTime(uuri, modifyTime, downLoadTime, false);
    }
    
    private boolean updateTime(String uuri, long modifyTime, long downLoadTime, boolean retry) throws DricException {
    	boolean result = false;
		try {
			result = updateTimeProxy(uuri, modifyTime, downLoadTime);
		} catch (DTException e) {
			throw new DricException(e, e.getErrorCode());
		} catch (TException e) {
			if(!retry){
				result = updateTime(uuri, modifyTime, downLoadTime, true);
			}else{
				throw new DricException(e, ErrorCode.T_TRANSPORT_ERROR);
			}
		}
		return result;
    }
    
	public void ping(String server, int port) throws DTException, TException {
		pingProxy(server, port);
		
	}
}
