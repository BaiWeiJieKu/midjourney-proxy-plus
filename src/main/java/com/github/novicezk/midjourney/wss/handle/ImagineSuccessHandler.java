package com.github.novicezk.midjourney.wss.handle;


import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * imagine消息处理.
 * 完成(create): **cat** - <@1012983546824114217> (relaxed)
 */
@Component
@Slf4j
public class ImagineSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";

	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		log.info("ImagineSuccessHandler.handle--->content:{}",content);
		ContentParseData parseData = ConvertUtils.parseContent(content, CONTENT_REGEX);
		if (MessageType.CREATE.equals(messageType) && parseData != null && hasImage(message)) {

			TaskCondition condition = new TaskCondition()
					.setActionSet(Set.of(TaskAction.IMAGINE))
					.setFinalPromptEn(parseData.getPrompt());
			log.info("ImagineSuccessHandler.handle--->findAndFinishImageTask---content:{}",content);
			findAndFinishImageTask(condition, parseData.getPrompt(), message);
		}
	}

}
