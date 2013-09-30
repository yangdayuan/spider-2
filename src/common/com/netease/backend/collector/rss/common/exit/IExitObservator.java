/**
 * 
 */
package com.netease.backend.collector.rss.common.exit;

/**
 * 进程退出反被观察者接口（在ExitObservable发出通知后将定时不断的循环调用isComplete接口直到返回true或者超时）
 * 实现此接口的观察者类isComplete方法实现必须简单，不应出现循环或者阻塞。
 * 如果isComplete有效则不必回调ExitObservable的complete方法
 * @author wuliufu
 */
public interface IExitObservator extends IExitObserver {
	/**
	 * 如果该对象已经完成退出则置为true，反之false
	 * @return 默认必须返回false,只有完成退出前所有操作后才能返回true
	 */
	boolean isComplete();
}
