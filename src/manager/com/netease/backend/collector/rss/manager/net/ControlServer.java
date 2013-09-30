/**
 * 
 */
package com.netease.backend.collector.rss.manager.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import com.netease.backend.collector.rss.common.consts.RunState;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.common.meta.URLInfo;
import com.netease.backend.collector.rss.common.net.DTException;
import com.netease.backend.collector.rss.common.net.TControlService;
import com.netease.backend.collector.rss.common.net.TURLInfo;
import com.netease.backend.collector.rss.common.net.TUrlInfoResult;
import com.netease.backend.collector.rss.common.util.DricUtil;
import com.netease.backend.collector.rss.common.util.ServerThread;
import com.netease.backend.collector.rss.manager.meta.UrlInfoResult;
import com.netease.backend.collector.rss.manager.service.ManagerService;
/**
 * CC监听服务类
 * @author wuliufu
 * @since 2010-12-02
 */
public class ControlServer {
	private static final Logger logger = Logger.getLogger(ControlServer.class);
	
	private String ip;

	// 监听端口
	private short port;

	// 停止延时,单位秒
	private int stopTimeOut = 5;

	// 线程池最小线程数
	private int minThreads = 5;

	// 线程池最大线程数
	private int maxThreads = Integer.MAX_VALUE;

	private TServer server = null;

	private ServerThread serverThread = null;
	
	private volatile RunState runState = RunState.READY;

	/**
	 * 监听的ip和端口
	 * @param ip 监听的ip
	 * @param port 监听的端口
	 */
	public ControlServer(String ip, short port) {
		this.ip = ip;
		this.port = port;
	}
	
	public boolean start() throws DricException {
		boolean result = false;
		try {
			ControlHandler handler = new ControlHandler();
			TControlService.Processor<TControlService.Iface> processor = new TControlService.Processor<TControlService.Iface>(
					handler);
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(ip, port));
			TServerTransport serverTransport = new TServerSocket(serverSocket);
			Args args = new Args(serverTransport);
			args.maxWorkerThreads = maxThreads;
			args.minWorkerThreads = minThreads;
			args.stopTimeoutUnit = TimeUnit.SECONDS;
			args.stopTimeoutVal = stopTimeOut;
			server = new TThreadPoolServer(args.processor(processor));
			serverThread = new ServerThread(server);
			serverThread.start();
			runState = RunState.RUNNING;
			logger.info("Control service have begun. Binding IP : " + ip + " & port : " + port);
			result = true;
		} catch (IOException e) {
			logger.error(String.format("localIp = %s, port = %d", ip, port));
			throw new DricException("can't create TServerTransport.", e, ErrorCode.INVALID_CODE);
		}
		return result;
	}
	
	public boolean stop() {
		if (server != null) {
			server.stop();
			runState = RunState.STOP;
			new Thread() {
				public void run() {
					while (server.isServing()) {
						try {
							Thread.sleep(100);
						} catch (Throwable t) {
						}
					}
					runState = RunState.TERMINATED;
				}
			}.start();
			logger.info("Control service will be stoped.");
			return true;
		}
		return false;
	}
	
	/**
	 * 通信服务是否终止，调用 之前必须已经完成对stop方法的调用
	 * @return 通信终止返回true，反之false
	 */
	public boolean isTerminated() {
		return runState == RunState.TERMINATED;
	}

    /**
     * 服务接口
     */
	private class ControlHandler implements TControlService.Iface {

		@Override
		public List<TURLInfo> requestURL() throws DTException, TException {
			//logger.debug("==> Entered ControlServer requestURL");
			
			List<URLInfo> urlInfos = ManagerService.getInstance().requestURL();
			List<TURLInfo> turlInfos = new LinkedList<TURLInfo>();
			for (URLInfo urlInfo: urlInfos) {
				TURLInfo turlInfo = DricUtil.URLInfo2TURLInfo(urlInfo);
				if (turlInfo == null) {
					continue;
				}
				turlInfos.add(turlInfo);
			}
			
			//logger.debug("==> Left ControlServer requestURL && size = " + turlInfos.size());
			return turlInfos;
		}

        /**
         * 添加urlInfos到等待队列
         * @param turlInfos urlinfos
         * @return 相同url不同via的urlInfo列表
         * @throws DTException
         * @throws TException
         */
		@Override
		public TUrlInfoResult sendURL(List<TURLInfo> turlInfos) throws DTException,
				TException {
			List<URLInfo> urlInfos = new LinkedList<URLInfo>();
			
			//logger.debug("==> Entered ControlServer sendURL");
			for (TURLInfo turlInfo: turlInfos) {
				URLInfo urlInfo = DricUtil.TURLInfo2URLInfo(turlInfo);
				if (urlInfo == null) {
					continue;
				}
				urlInfos.add(urlInfo);
				
				//logger.debug("ControlServer sendURL: " + urlInfo.getUurl() + "&& attach size:" + urlInfo.getAttach().length);
			}
			
			UrlInfoResult urlInfoResult = ManagerService.getInstance().sendURL(urlInfos);
			List<TURLInfo> exitDiffViaUrlInfos = new ArrayList<TURLInfo>(urlInfoResult.getExitDiffViaUrlInfos().size());
			List<TURLInfo> pendingURLInfos = new ArrayList<TURLInfo>(urlInfoResult.getPendingURLInfos().size());
			for (URLInfo exitDiffViaUrlInfo: urlInfoResult.getExitDiffViaUrlInfos()) {
				exitDiffViaUrlInfos.add(DricUtil.URLInfo2TURLInfo(exitDiffViaUrlInfo));
			}
			for (URLInfo pendingURLInfo : urlInfoResult.getPendingURLInfos()) {
				pendingURLInfos.add(DricUtil.URLInfo2TURLInfo(pendingURLInfo));
			}
			//logger.debug("==> Left ControlServer sendURL");
			return new TUrlInfoResult(exitDiffViaUrlInfos, pendingURLInfos);
		}


		@Override
		public boolean updateTime(String uuri, long modifyTime,
				long downLoadTime) throws DTException, TException {
			return ManagerService.getInstance().updateTime(uuri, modifyTime, downLoadTime);
		}

        @Override
		public void ping(String server, int port)
				throws TException {
		}

		
	}
	
}
