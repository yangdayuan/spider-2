package com.netease.backend.collector.rss.common.client.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.netease.backend.collector.rss.common.client.domain.PoolSocket;
import com.netease.backend.collector.rss.common.client.provider.ConnectionProvider;

/**
 * 同步代理工厂,动态代理方式
 * @author wangfuping
 */
public class SynProxyhandle implements InvocationHandler {

	private static Logger logger = LoggerFactory.getLogger(SynProxyhandle.class);
	
	private ThreadLocal<PoolSocket>  curPoolSoceket = new ThreadLocal<PoolSocket>();

	public ConnectionProvider connectionProvider;

	private Object obj;

	public SynProxyhandle(ConnectionProvider connectionProvider) {
		super();
		this.connectionProvider = connectionProvider;
	}


	public Object getProxy(String className) {
		try {
			obj = Class.forName(className).newInstance();
			return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj
					.getClass().getInterfaces(), this);
		} catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException:" + e.getMessage());  
		} catch (InstantiationException e) {
			  logger.error("InstantiationException:" + e.getMessage());  
		} catch (IllegalAccessException e) {
			  logger.error("IllegalAccessException:" + e.getMessage());  
		}
		return null;
	}


	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		Object result = null;
		PoolSocket poolSocket = null;
		boolean clearConn = false;
			try {
				if(!method.getName().equals("setProxyHandle")){
					poolSocket =  connectionProvider.getConnection();
					curPoolSoceket.set(poolSocket);
				}
				result = method.invoke(obj, args);
				return result;
			} catch(InvocationTargetException e){
				//这里用于捕获connection reset的异常,获取连接请求,后服务端意外中断
				if(e.getTargetException() instanceof TException){
					clearConn = true;
				}
				throw e.getTargetException();
			 } catch (Exception e) {
				  throw e;
			 }finally {
				 if(clearConn && poolSocket!=null){
					 connectionProvider.clearCon(poolSocket);
				 }else if( poolSocket!=null) {
					 connectionProvider.returnCon(poolSocket);
				}
			}

	}
	
    public TSocket getSocket()  { 
    	PoolSocket poolSocket = curPoolSoceket.get();
    	curPoolSoceket.remove();
    	TSocket tSocket = poolSocket.gettSocket();
        return tSocket;  
    }  
	
    
    public ConnectionProvider getConnectionProvider()   {  
        return connectionProvider;  
    }  
    
    public void setConnectionProvider(ConnectionProvider connectionProvider)  {  
        this.connectionProvider = connectionProvider;  
    }  

}
