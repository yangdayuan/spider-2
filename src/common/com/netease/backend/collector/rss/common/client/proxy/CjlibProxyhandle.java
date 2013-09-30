package com.netease.backend.collector.rss.common.client.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TSocket;
import com.netease.backend.collector.rss.common.client.domain.PoolSocket;
import com.netease.backend.collector.rss.common.client.provider.ConnectionProvider;

/**
 * 同步代理工厂,动态代理方式
 * @author wangfuping
 */
public class CjlibProxyhandle implements MethodInterceptor {
	
   private Enhancer enhancer = new Enhancer();
	
	private ThreadLocal<PoolSocket>  curPoolSoceket = new ThreadLocal<PoolSocket>();

	public ConnectionProvider connectionProvider;

	public CjlibProxyhandle(ConnectionProvider connectionProvider) {
		super();
		this.connectionProvider = connectionProvider;
	}


	 @SuppressWarnings("unchecked")
	public Object getProxy(Class clazz){
		  //设置需要创建子类的类
		  enhancer.setSuperclass(clazz);
		  enhancer.setCallback(this);
		  //通过字节码技术动态创建子类实例
		  return enhancer.create();
	 }

	 //实现MethodInterceptor接口方法
	public Object intercept(Object obj, Method method, Object[] args,MethodProxy proxy) throws Throwable {
		Object result = null;
		PoolSocket poolSocket = null;
		boolean clearConn = false;
			try {
				if(method.getName().endsWith("Proxy")){
					poolSocket =  connectionProvider.getConnection();
					curPoolSoceket.set(poolSocket);
				}
				  //通过代理类调用父类中的方法
                result = proxy.invokeSuper(obj, args);
				return result;
			} catch(InvocationTargetException e){
				//这里用于捕获connection reset的异常,获取连接请求,后服务端意外中断
				if(e.getTargetException() instanceof TException){
					clearConn = true;
				}
				throw e.getTargetException();
			 } catch (Exception e) {
				   if(e instanceof TException){
						clearConn = true;
				  }
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
