/**
 * 
 */
package com.netease.backend.collector.rss.common.exit;

import java.io.Serializable;

import com.netease.backend.collector.rss.common.exception.DricException;

/**
 * 进程退出观察者接口
 * 在接收到退出通知后，进行后续处理，处理完毕后必须回调ExitObservable的complete接口
 * @author wuliufu
 */
public interface IExitObserver extends Serializable {
	
	/**
	 * 接受准备退出通知
	 * @param o 被观察着对象
	 * @param args 附加参数
	 * @throws DricException
	 */
	void notifyExit(final ExitObservable o, Object args) throws DricException;
}
