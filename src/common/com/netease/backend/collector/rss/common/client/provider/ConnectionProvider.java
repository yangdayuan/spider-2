package com.netease.backend.collector.rss.common.client.provider;

import java.util.List;
import org.apache.commons.pool.ObjectPool;

import com.netease.backend.collector.rss.common.client.domain.PoolSocket;
public interface ConnectionProvider {  
    /** 
     * 取链接池中的一个链接 
     *  
     * @return 
     */  
    public PoolSocket getConnection() throws Exception;  
    
    /** 
     * 清空链接
     *  
     * @param socket 
     */  
    public void clearCon(PoolSocket poolSocket);  
    
    /** 
     * 返回链接 
     *  
     * @param socket 
     */  
    public void returnCon(PoolSocket poolSocket);  
    
    
    /**
     * 获取连接池列表
     * @return
     */
    public List<ObjectPool> getObjectPools();
}  