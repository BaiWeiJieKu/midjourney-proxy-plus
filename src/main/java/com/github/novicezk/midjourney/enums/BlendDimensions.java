package com.github.novicezk.midjourney.enums;


/**
 * 混合尺寸
 * @author qinfen
 * @date 2024/01/29
 */
public enum BlendDimensions {

	/**
	 * 2：3
	 */
	PORTRAIT("2:3"),

	/**
	 * 1：1
	 */
	SQUARE("1:1"),

	/**
	 * 3：2
	 */
	LANDSCAPE("3:2");

	private final String value;

	BlendDimensions(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
}
