package com.github.novicezk.midjourney.service;

import java.util.regex.Pattern;

/**
 *
 * 翻译服务
 * @author qinfen
 * @date 2024/01/25
 */
public interface TranslateService {

	/**
	 * 翻译为英文
	 * @param prompt 提示词
	 * @return {@link String}
	 */
	String translateToEnglish(String prompt);

	/**
	 * 是否包含中文
	 * @param prompt 提示词
	 * @return boolean
	 */
	default boolean containsChinese(String prompt) {
		return Pattern.compile("[\u4e00-\u9fa5]").matcher(prompt).find();
	}

}
