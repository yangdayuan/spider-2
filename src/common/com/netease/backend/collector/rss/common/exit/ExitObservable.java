/**
 * 
 */
package com.netease.backend.collector.rss.common.exit;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.netease.backend.collector.rss.common.exception.DricException;

/**
 * 采用观察者模式设计节点平滑安全退出
 * 平滑退出被观察者,单例实现
 * 安全退出原理：
 * 该实例启动后监听捕捉SIGTERM信号，然后触发信号处理线程处理后续业务，
 * 该信号处理最后调用System.exit发送通知退出给其他后续需要接收信号的业务
 * 实现流程：
 * 需要在程序退出之前执行完相关任务或释放资源者，
 * 必须实现IExitObserver或者IExitObservator接口并把该对象加入到ExitObservable实例中。
 * 退出延迟处理流程：
 * 1.此实例通知所有实现了IExitObserver或者IExitObservator接口的观察着对象准备退出
 * 2.IExitObserver或者IExitObservator实例在收到退出通知后必须停止接受新的请求，并处理正在排队的任务，处理完成后进行3操作
 * 3.IExitObservator实例需要在完成退出后isComplete方法能够返回true，其他时刻返回false；
 * IExitObserver实例必须回调此实例的complete通知完成退出
 * 4.此实例检查所有观察者实例是否都已经完成退出或者超时
 * 5.删除.run,并退出
 * 默认参数:
 * 超时默认为一分钟，及发出通知一分钟后，如果还没有回调就默认已经回调
 * @author wuliufu
 */
public class ExitObservable {
	private static final Logger logger = LoggerFactory.getLogger(ExitObservable.class);
	/**
	 * 单例实例
	 */
	private static ExitObservable instance = new ExitObservable();
	/**
	 * 是否退出
	 */
	private boolean isExit = false;
	/**
	 * 超时时间，单位毫秒
	 */
	private long timeout = DEFAULT_TIMEOUT;
	
	/**
	 * 默认超时时间，5分钟
	 */
	private static final long DEFAULT_TIMEOUT = 5 * 60 * 1000;
	/**
	 * 观察者列表
	 */
	private List<ExitObserverInfo> exitObservers = new LinkedList<ExitObserverInfo>();
	
	static {
		
		/**
		 * 拦截并处理15信号
		 */
		Signal.handle(new Signal("TERM"), new TermSignalHandler());
		
		/**
		 * 实现钩子线程,在终止处理结束后执行System.exit后触发
		 */
		Runtime.getRuntime().addShutdownHook(new Thread() {

			@Override
			public void run() {
//				getInstance().release();
				logger.info("钩子线程执行-version-121219。");
			}

		});
	}
	
	
	/**
	 * 终止信号处理
	 * @author wuliufu
	 *
	 */
	private static final class TermSignalHandler implements SignalHandler {

		@Override
		public void handle(Signal arg0) {
			try {
				getInstance().release();
			} catch(Throwable t) {
				logger.error("", t);
			} finally {
				System.exit(0);
			}
		}
		
	}
	
	private ExitObservable() {		
	}
	
		
	/**
	 * 获取单例实例
	 * @return instance 单例实例
	 */
	public static ExitObservable getInstance() {
		return instance;
	}


	/**
	 * 清理资源入口
	 */
	private void release() {
		try {
			//设置为退出状态，此时不再接受添加观察者请求
			isExit = true;
			
			logger.debug("Start release resources and willing exit.");
			//通知所有观察者
			notifyObservers();
			
			/*
			 * 检查所有观察者，直到所有观察者已完成退出或者超时
			 */
			checkComplete();
			
			logger.info("Application exit now.");
		} catch(Throwable t) {
			logger.error("", t);
		}
//		System.exit(0);
	}
		
	/**
	 * 回调接口，告诉被观察者通知处理完成,如果没有回调，
	 * 则在超过超时时间后默认已经完成处理
	 * @param o 观察者对象
	 */
	public void complete(IExitObserver o) {
		ExitObserverInfo info = new ExitObserverInfo(o);
		int index = exitObservers.indexOf(info);
		if(index != -1) {
			exitObservers.get(index).complete = true;
		}
	}
		
	/**
	 * 添加观察者
	 * @param o 待添加观察者对象
	 * @return 添加成功返回true，失败返回false
	 */
	public boolean addObserver(IExitObserver o) {
		if(isExit) {
			return false;
		}
	
		ExitObserverInfo info = new ExitObserverInfo(o);
		if(!exitObservers.contains(info))
			return exitObservers.add(info);
		return true;
	}
	
	/**
	 * 移除观察者
	 * @param o 待移除观察者对象
	 * @return 移除成功返回true，失败返回false
	 */
	public boolean removeObserver(IExitObserver o) {
		if(isExit) {
			return false;
		}
		
		ExitObserverInfo info = new ExitObserverInfo(o);
		return exitObservers.remove(info);
	}
	
	/**
	 * 通知所有观察者准备退出
	 */
	private void notifyObservers() {
		notifyObservers(null);
	}
	
	/**
	 * 通知所有观察者准备退出
	 * @param args 附加参数
	 */
	private void notifyObservers(Object args) {
		/*
		 * 通知所有观察者准备退出
		 */
		for(int i = 0; i < exitObservers.size(); i++)  {
			IExitObserver o = exitObservers.get(i).getObserver();
			try {
				o.notifyExit(this, args);
			} catch (DricException e) {
				logger.error(String.format("Can't notify exit to observer %s.", o.toString()), e);
			}
		}
	}
	
	/**
	 * 轮询所有观察者，直到所有观察者完成退出或者超时
	 */
	private void checkComplete() {
		//从当前时间开始计算超时
		long startTime = System.currentTimeMillis();
		
		while(!exitObservers.isEmpty() && (timeout == -1 || (System.currentTimeMillis() - startTime) < timeout)) {
			for(int i = 0; i < exitObservers.size(); i++) {
				ExitObserverInfo info = exitObservers.get(i);
				//优先检查complete属性
				boolean c = exitObservers.get(i).complete;
				/*
				 * 如果该属性为false且观察者是反被观察者
				 */
				if(!c && info.getObserver() instanceof IExitObservator) {
					//询问观察者是否完成退出
					c = ((IExitObservator)info.getObserver()).isComplete();
				}
				
				/*
				 * 如果观察者完成退出，移除该观察者
				 */
				if(c) {
					exitObservers.remove(i--);
				}
			}
			
			//休眠50ms
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}

	}
	
	/**
	 * 观察者数
	 * @return
	 */
	public int observerCount() {
		return exitObservers.size();
	}
	
	/**
	 * 观察则列表是否为空
	 * @return
	 */
	public boolean isEmpty() {
		return exitObservers.isEmpty();
	}

	/**
	 * 获取超时时间，单位毫秒，-1表示不超时
	 * @return timeout 超时时间，单位毫秒，-1表示不超时
	 */
	public long getTimeout() {
		return timeout;
	}

	/**
	 * 设置超时时间，单位毫秒，-1表示不超时
	 * @param timeout 超时时间，单位毫秒，-1表示不超时
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	
	/**
	 * 观察者信息封装类
	 * @author wuliufu
	 */
	public static class ExitObserverInfo {
		/**
		 * 观察者对象
		 */
		private IExitObserver observer;
		/**
		 * 是否完成退出操作
		 */
		private boolean complete = false;
		public ExitObserverInfo(IExitObserver observer) {
			this.observer = observer;
		}
		
		/**
		 * 获取观察者对象
		 * @return observer 观察者对象
		 */
		public IExitObserver getObserver() {
			return observer;
		}

		/**
		 * 获取是否完成退出操作
		 * @return complete 是否完成退出操作
		 */
		public boolean isComplete() {
			return complete;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((observer == null) ? 0 : observer.hashCode());
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ExitObserverInfo other = (ExitObserverInfo) obj;
			if (observer == null) {
				if (other.observer != null)
					return false;
			} else if (!observer.equals(other.observer))
				return false;
			return true;
		}
		
	}
}
