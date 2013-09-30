package com.netease.backend.collector.rss.common.client.util;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.thrift.TServiceClient;
import org.apache.thrift.async.TAsyncClient;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingTransport;
import org.apache.thrift.transport.TTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 获取Clinet工具类-反射机制
 * @author user
 * @see http://blog.csdn.net/heiyeshuwu/article/details/6443090
 *
 */
public class ClientUtil {
	
	private static Logger logger = LoggerFactory.getLogger(ClientUtil.class);
	
	
	/**
	 * 采用反射机制同步Client-TServiceClient
	 * @param protocol 传输通信协议类型:TBinaryProtocol\TCompactProtocol\...
	 * @param client 客户端类型:同步或异步客户端类型
	 * @param socket 传输方式对应的连接对象
	 * @return TServiceClient
	 */
	public static TServiceClient getTServiceClient(Class<?> protocol,
			Class<?> client, TTransport socket) {
		TServiceClient tServiceClient = null;
		try {
			if (protocol == null || client == null || socket == null) {
				return tServiceClient;
			}
			Constructor<?> protocolConstructor = protocol.getDeclaredConstructor(TTransport.class);
			TProtocol tProtocol = (TProtocol) protocolConstructor.newInstance(socket);

			Constructor<?> clientConstructor = client.getDeclaredConstructor(TProtocol.class);
			tServiceClient = (TServiceClient) clientConstructor.newInstance(tProtocol);
		} catch (SecurityException e) {
		   logger.error("SecurityException：" + e.getMessage());
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException：" + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException：" + e.getMessage());
		} catch (InstantiationException e) {
			logger.error("InstantiationException：" + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException：" + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException：" + e.getMessage());
		}
		return tServiceClient;
	}
	
	/**
	 * 采用反射机制同步Client-TServiceClient
	 * @param transport  传输方式类型:TSocket\TFramedTransport\TFileTransport
	 * @param protocol 传输通信协议类型:TBinaryProtocol\TCompactProtocol\...
	 * @param client 客户端类型:同步或异步客户端类型
	 * @param socket 传输方式对应的连接对象
	 * @return TServiceClient
	 */
	public static TServiceClient getTServiceClient(Class<?> transport, Class<?> protocol,
			Class<?> client, TTransport socket) {
		TServiceClient tServiceClient = null;
		try {
			if (transport == null || protocol == null || client == null || socket == null) {
				return tServiceClient;
			}
			Constructor<?> portConstructor = transport.getDeclaredConstructor(TTransport.class);
			portConstructor.setAccessible(true);
			TTransport tTransport = (TTransport) portConstructor.newInstance(socket);

			Constructor<?> protocolConstructor = protocol.getDeclaredConstructor(TTransport.class);
			TProtocol tProtocol = (TProtocol) protocolConstructor.newInstance(tTransport);
			Constructor<?> clientConstructor = client.getDeclaredConstructor(TProtocol.class);
			tServiceClient = (TServiceClient) clientConstructor.newInstance(tProtocol);
		} catch (SecurityException e) {
		   logger.error("SecurityException："+ e.getMessage());
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException：" + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException：" + e.getMessage());
		} catch (InstantiationException e) {
			logger.error("InstantiationException：" + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException：" + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException：" + e.getMessage());
		}
		return tServiceClient;
	}
	
	/**
	 * 采用反射机制获取异步Client-TAsyncClient
	 * @param protocolFactory 传输协议工厂类
	 * @param client 客户端类型:同步或异步客户端类型
	 * @param socket 传输工厂类型对应的连接对象
	 * @return
	 */
	public static TAsyncClient getTAsyncClient(Class<?> protocolFactory, Class<?> client,TTransport socket) {
		TAsyncClient tAsyncClient = null;
		try {
			if (protocolFactory == null ||client==null|| socket == null) {
				return tAsyncClient;
			}
			TAsyncClientManager clientManager = new TAsyncClientManager();
			TProtocolFactory tprotocolFactory=(TProtocolFactory) protocolFactory.newInstance();
			Constructor<?> clientConstructor = client.getDeclaredConstructor(TProtocolFactory.class,TAsyncClientManager.class,TNonblockingTransport.class);
			tAsyncClient = (TAsyncClient) clientConstructor.newInstance(tprotocolFactory,clientManager,socket);
		} catch (SecurityException e) {
		   logger.error("SecurityException：" + e.getMessage());
		} catch (NoSuchMethodException e) {
			logger.error("NoSuchMethodException：" + e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("IllegalArgumentException：" + e.getMessage());
		} catch (InstantiationException e) {
			logger.error("InstantiationException：" + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException：" + e.getMessage());
		} catch (InvocationTargetException e) {
			logger.error("InvocationTargetException：" + e.getMessage());
		} catch (IOException e) {
			logger.error("IOException：" + e.getMessage());
		}
		return tAsyncClient;
	}

}
