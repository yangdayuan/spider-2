package com.netease.backend.collector.rss.common.client.service;

import java.util.List;

import org.apache.thrift.TException;

import com.netease.backend.collector.rss.common.client.proxy.CjlibProxyhandle;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.net.DTException;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.net.TUrlInfoResult;

/**
 * 中心节点客户端业务接口,采用连接池实现
 * 
 * @author wangfuping
 * 
 */
public interface ControlService {

	/**
	 * 用来设置动态代理handle
	 * @param proxyhandle
	 */
	public void setProxyHandle(CjlibProxyhandle proxyhandle);

	/**
	 * 请求待抓取链接
	 * @return
	 * @throws DricException
	 */
	public List<TURLInfo> requestURL() throws DricException;

	/**
	 * 发送需要抓取的链接
	 * @param urlInfos
	 * @return
	 * @throws DricException
	 */
	public TUrlInfoResult sendURL(List<TURLInfo> urlInfos) throws DricException;

	/**
	 * 更新链接资源的下载时间
	 * @param uuri
	 * @param modifyTime
	 * @param downLoadTime
	 * @return
	 * @throws DricException
	 */
	public boolean updateTime(String uuri, long modifyTime, long downLoadTime) throws DricException;

	public void ping(String server, int port) throws DTException, TException;

}
