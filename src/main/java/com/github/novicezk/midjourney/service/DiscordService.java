package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.result.Message;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

/**
 * discord操作接口
 * @author qinfen
 * @date 2024/01/30
 */
public interface DiscordService {

	/**
	 * 文生图命令
	 * @param prompt 提示词
	 * @param nonce 仅一次有效的随机字符串
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> imagine(String prompt, String nonce);

	/**
	 * 放大某一张图片
	 * @param messageId
	 * @param index
	 * @param messageHash
	 * @param messageFlags
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> upscale(String messageId, int index, String messageHash, int messageFlags, String nonce);

	/**
	 * 放大某一张图片的像素
	 * @param messageId
	 * @param num
	 * @param messageHash
	 * @param messageFlags
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> upscaleX(String messageId, int num, String messageHash, int messageFlags, String nonce);

	/**
	 * 缩放一张图并补充细节生成四张图
	 * @param messageId
	 * @param num
	 * @param messageHash
	 * @param messageFlags
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> outpaintX(String messageId, int num, String messageHash, int messageFlags, String nonce);

	/**
	 * 基于某张图再生成一组图
	 * @param messageId
	 * @param index
	 * @param messageHash
	 * @param messageFlags
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> variation(String messageId, int index, String messageHash, int messageFlags, String nonce);

	Message<Void> variationLow(String messageId, String messageHash, int messageFlags, String nonce);

	Message<Void> variationHigh(String messageId, String messageHash, int messageFlags, String nonce);

	/**
	 * 重新生成一组图
	 * @param messageId
	 * @param messageHash
	 * @param messageFlags
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> reroll(String messageId, String messageHash, int messageFlags, String nonce);

	/**
	 * 图生文
	 * @param finalFileName
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> describe(String finalFileName, String nonce);

	/**
	 * 混图，垫图
	 * @param finalFileNames
	 * @param dimensions
	 * @param nonce
	 * @return {@link Message}<{@link Void}>
	 */
	Message<Void> blend(List<String> finalFileNames, BlendDimensions dimensions, String nonce);

	/**
	 * 上传
	 * @param fileName
	 * @param dataUrl
	 * @return {@link Message}<{@link String}>
	 */
	Message<String> upload(String fileName, DataUrl dataUrl);

	/**
	 * 发送图片消息
	 * @param content
	 * @param finalFileName
	 * @return {@link Message}<{@link String}>
	 */
	Message<String> sendImageMessage(String content, String finalFileName);

	Message<Void> direction(String direction,String messageId, String messageHash, int messageFlags, String nonce);

}
