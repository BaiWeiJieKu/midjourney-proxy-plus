package com.github.novicezk.midjourney.exception;

/**
 * 雪花算法异常
 * @author qinfen
 * @date 2024/01/29
 */
public class SnowFlakeException extends RuntimeException {

	/**
	 * 雪花算法异常
	 * @param message
	 */
	public SnowFlakeException(String message) {
		super(message);
	}

	/**
	 * 雪花算法异常
	 * @param message
	 * @param cause
	 */
	public SnowFlakeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 雪花算法异常
	 * @param cause
	 */
	public SnowFlakeException(Throwable cause) {
		super(cause);
	}
}
