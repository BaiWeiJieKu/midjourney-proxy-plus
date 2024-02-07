package com.github.novicezk.midjourney.wss.handle;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.loadbalancer.DiscordLoadBalancer;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;

import javax.annotation.Resource;
import java.util.Comparator;

/**
 * MJ消息处理
 * @author qinfen
 * @date 2024/01/26
 */
@Slf4j
public abstract class MessageHandler {
	@Resource
	protected DiscordLoadBalancer discordLoadBalancer;
	@Resource
	protected DiscordHelper discordHelper;

	/**
	 *  消息处理
	 * @param messageType 消息类型
	 * @param message 消息
	 */
	public abstract void handle(MessageType messageType, DataObject message);

	/**
	 * 获取消息内的content
	 * @param message 消息体
	 * @return {@link String}
	 */
	protected String getMessageContent(DataObject message) {
		return message.hasKey("content") ? message.getString("content") : "";
	}

	/**
	 * 从消息体中获取nonce
	 * @param message 消息体
	 * @return {@link String}
	 */
	protected String getMessageNonce(DataObject message) {
		return message.hasKey("nonce") ? message.getString("nonce") : "";
	}

	protected void findAndFinishImageTask(TaskCondition condition, String finalPrompt, DataObject message) {
		String imageUrl = getImageUrl(message);
		String messageHash = this.discordHelper.getMessageHash(imageUrl);
		condition.setMessageHash(messageHash);
		Task task = this.discordLoadBalancer.findRunningTask(condition)
				.findFirst().orElseGet(() -> {
					condition.setMessageHash(null);
					return this.discordLoadBalancer.findRunningTask(condition)
							.min(Comparator.comparing(Task::getStartTime))
							.orElse(null);
				});
		if (task == null) {
			return;
		}
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, finalPrompt);
		task.setProperty(Constants.TASK_PROPERTY_MESSAGE_HASH, messageHash);
		task.setImageUrl(imageUrl);
		log.info("MessageHandler.findAndFinishImageTask-task:{}",task);
		finishTask(task, message);
		log.info("MessageHandler.findAndFinishImageTask-finishtask:{}",task);
		task.awake();
	}

	protected void finishTask(Task task, DataObject message) {
		task.setProperty(Constants.TASK_PROPERTY_MESSAGE_ID, message.getString("id"));
		task.setProperty(Constants.TASK_PROPERTY_FLAGS, message.getInt("flags", 0));
		task.setProperty(Constants.TASK_PROPERTY_MESSAGE_HASH, this.discordHelper.getMessageHash(task.getImageUrl()));
		task.success();
	}

	protected boolean hasImage(DataObject message) {
		DataArray attachments = message.optArray("attachments").orElse(DataArray.empty());
		return !attachments.isEmpty();
	}

	protected String getImageUrl(DataObject message) {
		DataArray attachments = message.getArray("attachments");
		if (!attachments.isEmpty()) {
			String imageUrl = attachments.getObject(0).getString("url");
			return replaceCdnUrl(imageUrl);
		}
		return null;
	}

	protected String replaceCdnUrl(String imageUrl) {
		if (CharSequenceUtil.isBlank(imageUrl)) {
			return imageUrl;
		}
		String cdn = this.discordHelper.getCdn();
		if (CharSequenceUtil.startWith(imageUrl, cdn)) {
			return imageUrl;
		}
		return CharSequenceUtil.replaceFirst(imageUrl, DiscordHelper.DISCORD_CDN_URL, cdn);
	}

}
