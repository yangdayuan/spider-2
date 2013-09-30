package com.netease.backend.collector.rss.common.exception;

/**
 * hdir项目中，抛出的所有的异常都为HDirException
 * @author User
 *
 */
public class DricException extends Exception {

	private static final long serialVersionUID = 6534234382760826716L;
	
	private int errorCode;

	public int getErrorCode() {
		return errorCode;
	}
	
	public DricException(String message, int errorCode) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public DricException(Throwable cause, int errorCode) {
		super(cause);
		this.errorCode = errorCode;
	}
	
	public DricException(String message, Throwable cause, int errorCode) {
		super(message, cause);
		this.errorCode = errorCode;
	}
	
	public DricException(String message) {
		super(message);
	}
	
	public DricException(Throwable cause) {
		super(cause);
	}
	
	public DricException(String message, Throwable cause) {
		super(message, cause);
	}
}
