package com.netease.backend.collector.rss.common.client.factory;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 同步池对象工厂,和TSocket对象相关
 * 
 * @author wangfuping
 * 
 */
public class SynPoolableObjectFactory implements PoolableObjectFactory {
	
	/** 日志记录器 */
	public static final Logger logger = LoggerFactory.getLogger(SynPoolableObjectFactory.class);
	
	/** host地址 */
	private String host;
	
	/** 超时设置 */
	private int timeOut;

	
	/**
	 * 
	 * @param host 服务器地址
	 * @param timeOut 超时时间,默认15秒
	 */
	public SynPoolableObjectFactory(String host,int timeOut) {
	    this.host = host;
	    this.timeOut = timeOut;
		
	}

	/**
	 * 池对象销毁
	 */
	@Override
	public void destroyObject(Object arg0) throws Exception {
		if (arg0 instanceof TSocket) {
			TSocket socket = (TSocket) arg0;
			if (socket.isOpen()) {
				socket.close();
			}
		}
	}
	

	/**
	 * 创建池对象
	 */
	@Override
	public Object makeObject() throws Exception {
		boolean succ = false;
		TTransport transport = null;
		while (!succ) {
			try {
				String[] iport = host.split(":");
				String ip = iport[0];
				int port = Integer.parseInt(iport[1]);
				transport = new TSocket(ip,port, this.timeOut);
		        transport.open();
		        succ = true;
			} catch (Exception e) {
				transport = null;
				succ = false;
				throw e;
			}
		}
        return transport;
	}


	/**
	 * 验证池对象的合法性,socket是否open状态
	 */
	@Override
	public boolean validateObject(Object arg0) {
		try {
			if (arg0 instanceof TSocket) {
				TSocket thriftSocket = (TSocket) arg0;
				if (thriftSocket.isOpen()) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void passivateObject(Object arg0) throws Exception {
		// DO NOTHING
	}

	@Override
	public void activateObject(Object arg0) throws Exception {
		// DO NOTHING
	}


	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

}
