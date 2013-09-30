/**
 * 
 */
package com.netease.backend.collector.rss.common.util;

import org.apache.log4j.Logger;
import org.apache.thrift.server.TServer;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;

/**
 * 锟斤拷锟斤拷锟斤拷锟斤拷锟竭筹拷锟斤拷
 * @author wuliufu
 * @since 2010-09-16
 */
public class ServerThread  extends Thread {
	private static final Logger logger=Logger.getLogger(ServerThread.class);
	private TServer server;

	public ServerThread(TServer server) throws DricException {
		if (server == null) {
			throw new DricException("TServer is null.",ErrorCode.INVALID_CODE);
		}
		this.server = server;
	}

	public void run() {
		try {
			server.serve();
		} catch (Throwable e) {
			logger.error("", e);
		}
	}
}