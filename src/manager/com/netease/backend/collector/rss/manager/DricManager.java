package com.netease.backend.collector.rss.manager;

import java.util.Timer;

import org.apache.log4j.Logger;

import com.netease.backend.collector.rss.common.bdb.BdbManager;
import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exit.ExitObservable;
import com.netease.backend.collector.rss.common.exit.ExitObserver;
import com.netease.backend.collector.rss.manager.config.ManagerConfig;
import com.netease.backend.collector.rss.manager.net.ControlServer;
import com.netease.backend.collector.rss.manager.service.ManagerService;

public class DricManager {
	
	private static final Logger logger = Logger.getLogger(DricManager.class);
	
	private URLManager urlManager = null;

	public void init() throws DricException {
		
		String dbPath = ManagerConfig.getInstance().getStateDbPath();
		int cachePercent = ManagerConfig.getInstance().getBdbCachePercent();
		BdbManager.getInstance().init(dbPath, cachePercent);
		urlManager = new URLManager();
		urlManager.init();
		
		long reloadUrlInterval = ManagerConfig.getInstance().getReloadUrlInterval();
		
		ManagerService.getInstance().init(urlManager);
		Timer refetchSeedTimer = new Timer();
		refetchSeedTimer.schedule(urlManager, 0, reloadUrlInterval);
	}
	
	public void unInit() {
		urlManager.unInit();
		BdbManager.getInstance().close();
	}
	
	public static void main(String[] args) throws DricException {
		if (args.length == 0) {
			logger.error("please set the config of manager path");
			return;
		}
		System.setProperty("logdir", "./ddblog-manager");
		ManagerConfig.getInstance().init(args[0]);

		final DricManager manager = new DricManager();
		manager.init();
		
		String serverIp = ManagerConfig.getInstance().getServerIp();
		short serverPort = ManagerConfig.getInstance().getServerPort();
		final ControlServer server = new ControlServer(serverIp, serverPort);
		server.start();
		
		ExitObservable.getInstance().addObserver(new ExitObserver() {
			private static final long serialVersionUID = -236003793007823720L;

			public void notifyExit(final ExitObservable o, Object args) throws DricException {
				super.notifyExit(o, args);
				final ExitObserver exitor = this;
				new Thread() {
					public void run() {
						try {
							server.stop();
							while(!server.isTerminated()) {
								try {
									Thread.sleep(10);
								} catch(Exception e) {
									logger.error("", e);
								}
							}
							manager.unInit();
							o.complete(exitor);
						} catch(Throwable t) {
							logger.error("", t);
						}
					}
				}.start();
			}
		});
		logger.info("==================服务启动成功================");
	}
}
