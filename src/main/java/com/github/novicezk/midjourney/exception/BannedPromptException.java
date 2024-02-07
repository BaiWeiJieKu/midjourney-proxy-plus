package com.github.novicezk.midjourney.exception;

/**
 * 提示词敏感异常
 * @author qinfen
 * @date 2024/01/29
 */
public class BannedPromptException extends Exception {

	/**
	 * 提示词敏感异常
	 * @param message
	 */
	public BannedPromptException(String message) {
		super(message);
	}
}
