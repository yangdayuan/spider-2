package com.netease.backend.collector.rss.common.client.provider.imp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.transport.TSocket;
import com.netease.backend.collector.rss.common.client.domain.PoolSocket;
import com.netease.backend.collector.rss.common.client.factory.SynPoolableObjectFactory;
import com.netease.backend.collector.rss.common.client.provider.ConnectionProvider;

/**
 * 同步连接对象提供类-动态代理方式
 * @author wangfuping
 *
 */
public class SynProxyConnectionProvider implements ConnectionProvider {
	
	
	/** 连接池列表 */
    private List<ObjectPool> objectPools = Collections.synchronizedList(new ArrayList<ObjectPool>());
    
	/** 轮询到池的位置 */
	private static int cur = 0;
	
	private boolean retryConstantly;
	
	/**清理idle对象的间隔时间*/
	private static final long TIME_BETWEEN_EVICTION_RUNS_MILLIS = 5 * 60 * 1000;
	
	/**每次清理需要检查多少个对象,默认是3*/
    public static final int NUM_TESTS_PER_EVICTION_RUN = 3;
	
	/**该参数决定idle是否应该被清理,默认30分钟为过期;决定过期的ilde对象被清理,无法保证剩余对象数量为minidle,只要过期,一律清理*/
    private long MIN_EVICTABLE_IDLE_TIME_MILLIS = 30 * 60 * 1000;
    
	/**该参数只有在MIN_EVICTABLE_IDLE_TIME_MILLIS<=0的情况下起作用;决定idle是否应该被清理,决定过期的idle对象被清理,可以保证剩余对象数量为minidle*/
	private static final long SOFT_MIN_EVICTABLE_IDLE_TIMEMILLIS = 30 * 60 * 1000;
	
	/**
	 * 构造连接对象提供类
	 * @param hostsList 服务器地址列表
	 * @param conTimeOut 连接超时时间,单位毫秒
	 */
	public SynProxyConnectionProvider(String[] hostsList, int conTimeOut ,int minIdle ,int maxActive) {
		super();
		for (String host : hostsList) {
			ObjectPool objectPool = new GenericObjectPool();
			((GenericObjectPool) objectPool).setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
			/** 缓存池中最小空闲对象数量 */
			((GenericObjectPool) objectPool).setMinIdle(minIdle);
			/** 缓存池中最大空闲对象数量 */
			((GenericObjectPool) objectPool).setMaxIdle(maxActive);
			/** 可以从缓存池中分配对象的最大数量 */
			((GenericObjectPool) objectPool).setMaxActive(maxActive);
			((GenericObjectPool) objectPool).setTestOnBorrow(true);
			((GenericObjectPool) objectPool).setTestOnReturn(true);
			((GenericObjectPool) objectPool).setTestWhileIdle(true);
			((GenericObjectPool) objectPool).setTimeBetweenEvictionRunsMillis(TIME_BETWEEN_EVICTION_RUNS_MILLIS);
			((GenericObjectPool) objectPool).setNumTestsPerEvictionRun(NUM_TESTS_PER_EVICTION_RUN);
			((GenericObjectPool) objectPool).setMinEvictableIdleTimeMillis(MIN_EVICTABLE_IDLE_TIME_MILLIS);
			((GenericObjectPool) objectPool).setSoftMinEvictableIdleTimeMillis(SOFT_MIN_EVICTABLE_IDLE_TIMEMILLIS);
			// 设置factory
			SynPoolableObjectFactory thriftPoolableObjectFactory = new SynPoolableObjectFactory(
					host, conTimeOut);
			objectPool.setFactory(thriftPoolableObjectFactory);
			objectPools.add(objectPool);
		}

	}

	/**
	 * 获取连接对象,轮循获取
	 * @throws Exception 
	 */
	@Override
	public synchronized PoolSocket getConnection() throws Exception {
		boolean succ = false;
		PoolSocket poolSocket = null;
		int errorCount =0;
		while (!succ) {
			if (cur >= objectPools.size()) {
				cur = 0;
			}
		     ObjectPool objectPool = objectPools.get(cur);
			try {
				TSocket tSocket= (TSocket) objectPool.borrowObject();
		        succ = true;
		        poolSocket = getPoolSocket(cur, tSocket);
				cur++;
			} catch (Exception e) {
				//这里用于捕获connection refused的异常,如服务端关闭,无法获取socket
				objectPool.clear();
				cur++;
				succ = false;
				errorCount++;
				if(retryConstantly == false &&errorCount >= objectPools.size()){
					throw e;
				}
			}
		}
	    return poolSocket;
	}
	
	
	private  PoolSocket getPoolSocket(int cur ,TSocket tSocket){
		PoolSocket  poolSocket  = new PoolSocket();
        poolSocket.setCur(cur);
        poolSocket.settSocket(tSocket);
        return poolSocket;
	}
    /**
     * 归还对象到池中
     */
	@Override
	public void returnCon(PoolSocket poolSocket) {
		try {
			ObjectPool objectPool = objectPools.get(poolSocket.getCur());
			if(objectPool != null ){
				objectPool.returnObject(poolSocket.gettSocket());
			}
		} catch (Exception e) {
			throw new RuntimeException("error returnCon()", e);
		}
	}
	
	@Override
	public void clearCon(PoolSocket poolSocket) {
		try {
			ObjectPool objectPool = objectPools.get(poolSocket.getCur());
			objectPool.invalidateObject(poolSocket.gettSocket());
		} catch (Exception e) {
			throw new RuntimeException("error clearCon()", e);
		}
		
	}


	/**
	 * 获取连接池列表
	 */
	@Override
	public List<ObjectPool> getObjectPools() {
		return objectPools;
	}

	public boolean isRetryConstantly() {
		return retryConstantly;
	}

	public void setRetryConstantly(boolean retryConstantly) {
		this.retryConstantly = retryConstantly;
	}
	

}
