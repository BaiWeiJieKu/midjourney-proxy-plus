package com.github.novicezk.midjourney.wss.user;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.github.novicezk.midjourney.domain.DiscordAccount;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class UserMessageListener {
	private final DiscordAccount account;
	private final List<MessageHandler> messageHandlers;

	public UserMessageListener(DiscordAccount account, List<MessageHandler> messageHandlers) {
		this.account = account;
		this.messageHandlers = messageHandlers;
	}

	public void onMessage(DataObject raw) {
		//log.info("UserMessageListener.onMessage:{}",raw.toString());
		MessageType messageType = MessageType.of(raw.getString("t"));
		log.info("UserMessageListener.onMessage messageType:{}",messageType);
		if (messageType == null || MessageType.DELETE == messageType) {
			return;
		}
		DataObject data = raw.getObject("d");
		//判断是不是此channel
		if (ignoreAndLogMessage(data, messageType)) {
			return;
		}
		ThreadUtil.sleep(50);
		for (MessageHandler messageHandler : this.messageHandlers) {
			log.info("UserMessageListener.onMessage--->messageHandler({}).handle",messageHandler.getClass());
			messageHandler.handle(messageType, data);
		}
	}

	private boolean ignoreAndLogMessage(DataObject data, MessageType messageType) {
		String channelId = data.getString("channel_id");
		log.info("UserMessageListener.ignoreAndLogMessage data.channelId:{},account.channelId:{}",channelId,this.account.getChannelId());
		if (!CharSequenceUtil.equals(channelId, this.account.getChannelId())) {
			return true;
		}
		String authorName = data.optObject("author").map(a -> a.getString("username")).orElse("System");
		log.info("UserMessageListener.ignoreAndLogMessage channel：{} - messageType:{} - authorName:{}: - data:{}", this.account.getDisplay(), messageType.name(), authorName, new String(data.toJson(), StandardCharsets.UTF_8));
		return false;
	}
}
