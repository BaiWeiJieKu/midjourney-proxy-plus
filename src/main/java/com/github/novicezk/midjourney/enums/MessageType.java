package com.github.novicezk.midjourney.enums;


/**
 * 消息类型
 * @author qinfen
 * @date 2024/01/29
 */
public enum MessageType {
	/**
	 * 创建.
	 */
	CREATE,
	/**
	 * 修改.
	 */
	UPDATE,
	/**
	 * 删除.
	 */
	DELETE;

	public static MessageType of(String type) {
		return switch (type) {
			case "MESSAGE_CREATE" -> CREATE;
			case "MESSAGE_UPDATE" -> UPDATE;
			case "MESSAGE_DELETE" -> DELETE;
			default -> null;
		};
	}
}
