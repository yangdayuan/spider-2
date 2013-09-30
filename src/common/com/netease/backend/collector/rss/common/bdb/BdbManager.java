package com.netease.backend.collector.rss.common.bdb;

import java.io.File;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.SynchronousQueue;

import org.apache.log4j.Logger;
import org.archive.util.bdbje.EnhancedEnvironment;

import com.netease.backend.collector.rss.common.exception.DricException;
import com.netease.backend.collector.rss.common.exception.ErrorCode;
import com.netease.backend.collector.rss.manager.bdb.Compare;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

/**
 * BDB数据库的管理类，主要是中心节点用于存储url信息值
 * @author XinDingfeng
 *
 */
public class BdbManager {
	private static final Logger logger = Logger.getLogger(BdbManager.class);
	private static final Byte DEFAULT_OBJECT = Byte.valueOf((byte)0);
	protected static final long DEFAULT_INTERVAL = 10 * 1000;
	
	private Environment env = null;
	
	private Map<String, BDataBase> dbMap = Collections.synchronizedMap(new HashMap<String, BDataBase>());
	private Map<String, Syncer> syncerMap = Collections.synchronizedMap(new HashMap<String, Syncer>(24));
	
	private static final BdbManager instance = new BdbManager();
	
	private BdbManager() {
	}
	
	private void startSyncer(final String name) {
		Syncer syncer = this.syncerMap.remove(name);
		if(syncer != null) {
			syncer.setStop(true);
			syncer.interrupt();
		}
		syncer = new Syncer(name);
		this.syncerMap.put(name, syncer);
		syncer.setDaemon(true);
		syncer.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error("Syncer get uncaughtException.", e);
				startSyncer(name);
			}
			
		});
		syncer.start();
	}
	
	public static BdbManager getInstance() {
		return instance;
	}
	
	protected class Syncer extends Thread {
		private boolean stop = false;
		private SynchronousQueue<Byte> queue = new SynchronousQueue<Byte>();
		private String name;
		
		public Syncer(String name) {
			this.name = name;
		}
		public void needSync() {
			queue.offer(DEFAULT_OBJECT);
		}

		public void run() {
			while(!stop) {
				try {
					Byte b = queue.take();
					if(b != null) {
						forceSync(name);
						logger.debug("Sync bdb with name= " + name);
					}
					Thread.sleep(DEFAULT_INTERVAL);
				} catch(Throwable t) {
					logger.error("", t);
				}
			}

		}
		/**
		 * 获取stop
		 * @return stop stop
		 */
		public boolean isStop() {
			return stop;
		}
		/**
		 * 设置stop
		 * @param stop stop
		 */
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		
	}
		
	/**
	 * 同步数据
	 */
	public void sync(String dbName) {
		Syncer syncer = this.syncerMap.get(dbName);
		if(syncer != null) {
			syncer.needSync();
		}
	}
	
	/**
	 * 同步数据
	 */
	private void forceSync(String dbName) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return;
		}
		
		bdb.sync();
	}
	
	private DatabaseConfig getDatabaseConfig() {
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setAllowCreate(true);
        dbConfig.setDeferredWrite(true);
        
        return dbConfig;
    }
	
	/**
	 * 初始化函数，并打开数据库，如果没有创建将自动创建
	 * @param dbPath 数据库路径
	 * @param cachePercent 数据库cache设置的百分比
	 * @throws DricException
	 */
	public void init(String dbPath, int cachePercent) throws DricException {
		File dbFile = new File(dbPath);
		if (!dbFile.exists()) {
			dbFile.mkdirs();
		}
		
		EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        if(cachePercent > 0 && cachePercent < 100) {
            envConfig.setCachePercent(cachePercent);
        }
        envConfig.setSharedCache(true);
        envConfig.setLockTimeout(5000000); // 5 seconds
        
        try {
        	env = new EnhancedEnvironment(dbFile, envConfig);
        } catch (Exception e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.BDB_INIT);
		}
	}
	
	public void open(String dbName, DatabaseConfig dbConfig, BdbEntryTransform transform) throws DricException {
		try {
			if (dbConfig == null) {
				dbConfig = getDatabaseConfig();
			}
			
			if (transform == null) {
				transform = new DefaultBdbEntryTransformImpl();
			}
			
			Database db = env.openDatabase(null, dbName, dbConfig);
			BDataBase bdb = new BDataBase(db, transform);
			dbMap.put(dbName, bdb);
			startSyncer(dbName);
		} catch (Exception e) {
			logger.error("", e);
			throw new DricException(e, ErrorCode.BDB_OPEN);
		}
    }
	
	/**
	 * 关闭数据库，考虑线程安全
	 */
	public synchronized void close(String dbName) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb != null) {
			bdb.close();
		    dbMap.remove(dbName);
		}
	}
	
	public synchronized void close() {
		Iterator<Entry<String, BDataBase>> iter = dbMap.entrySet().iterator();
		while (iter.hasNext()) {
			iter.next().getValue().close();
		}
		
		if (env != null) {
		    try {
		    	env.sync();
				env.close();
			} catch (DatabaseException e) {
				logger.error(e.getMessage());
			}
		}
	}
    
    /**
     * 放入(key,value)，如果key已经存在，不存储于数据库中，并返回false，如果key不存在，则存储于数据库中，返回true
     * @param key
     * @param value
     * @return 存储成功返回true，失败返回false
     */
	public boolean putNoOverwrite(String dbName, Object key, Object value) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return false;
		}
		
		return bdb.putNoOverwrite(key, value);
	}
	
	/**
	 * 将(key,value)存储数据库中，如果key已经存在，将替换原有的值
	 * @param key
	 * @param value
	 * @return 存储成功，返回true，否则返回false
	 */
	public boolean put(String dbName, Object key, Object value) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return false;
		}
		
		return bdb.put(key, value);
	}
	
	/**
	 * 根据key删除数据
	 * @param key
	 * @return 删除成功返回true，否则返回false
	 */
	public boolean remove(String dbName, Object key) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return false;
		}
        
		return bdb.remove(key);
	}
	
	/**
	 * 根据key获取value的值
	 * @param key
	 * @return 如果该key不存在，返回null；否则返回相对应的value值
	 */
	public Object get(String dbName, Object key) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return null;
		}
		
		return bdb.get(key);
	}
	
	public List<Object> getAllValues(String dbName) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return null;
		}
		
		return bdb.getAllValues();
	}
	
	public List<Object> getAllKeys(String dbName) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return null;
		}
		
		return bdb.getAllKeys();
	}
	
	/**
	 * 检查数据库中是否包含相应的key值
	 * @param key
	 * @return 存在返回true，否则返回false
	 */
	public boolean contain(String dbName, Object key) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return false;
		}
		
		return bdb.contain(key);
	}
	
	public List<Object> getKeyRange(String dbName, Object key, Compare cmp) {
		BDataBase bdb = dbMap.get(dbName);
		if (bdb == null) {
			return null;
		}
		
		return bdb.getKeyRange(key, cmp);
	}

}
