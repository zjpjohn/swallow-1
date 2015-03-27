package com.dianping.swallow.common.internal.exception;

/**
 * @author mengwenchao
 *
 * 2015年3月26日 下午2:24:10
 */
public class SwallowException extends Exception{

	private static final long serialVersionUID = 1L;

	public SwallowException(String message){
		super(message);
	}

	public SwallowException(String message, Throwable th){
		super(message, th);
	}

}
