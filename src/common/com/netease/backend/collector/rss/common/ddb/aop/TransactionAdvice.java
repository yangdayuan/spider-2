package com.netease.backend.collector.rss.common.ddb.aop;

import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import com.netease.backend.collector.rss.common.ddb.aop.annotation.Transaction;
import com.netease.dbsupport.transaction.IDBTransactionManager;

@Aspect
public class TransactionAdvice {
	private static final Logger logger = Logger
			.getLogger(TransactionAdvice.class);

	private IDBTransactionManager transactionManager;

	/**
	 * 事务增强器,在方法的开头结尾加上事务的开始提交回滚操作
	 */
	@Around("execution(* com.netease.backend.collector.rss.common.ddb.service..*.*(..)) && @annotation(transaction)")
	public Object invoke(ProceedingJoinPoint pjp, Transaction transaction)
			throws Throwable {
		if (getTransactionManager().getAutoCommit()) {
			Object result = null;
			String methodName = ((MethodSignature) pjp.getSignature())
					.getMethod().getName();
			try {
				getTransactionManager().setAutoCommit(false);
//				if (logger.isDebugEnabled()) {
//					logger.debug(methodName + "事务增强");
//				}
				result = pjp.proceed();
				if (result instanceof Boolean && !(Boolean) result)
					getTransactionManager().rollback();
				getTransactionManager().commit();
			} catch (Exception e) {
				logger.error("事务失败", e);
				getTransactionManager().rollback();

				throw e;
			} finally {
				getTransactionManager().setAutoCommit(true);
			}

//			if (logger.isDebugEnabled()) {
//				logger.debug(methodName + "事务增强结束");
//			}
			return result;
		} else {
			return pjp.proceed();
		}
	}

	public IDBTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(IDBTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}
}
