package com.github.novicezk.midjourney.wss.handle;

import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.ConvertUtils;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * outpaint1.5,outpaint2.
 * 完成(create): **a flying man** - Zoom Out by <@426580409665716235> (fast)
 */
@Component
public class OutpaintSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Zoom Out by <@\\d+> \\((.*?)\\)";


	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		ContentParseData parseData = ConvertUtils.parseContent(content, CONTENT_REGEX);
		if (MessageType.CREATE.equals(messageType) && parseData != null && hasImage(message)) {
			TaskCondition condition = new TaskCondition();
			if (content.contains("Zoom Out by")) {
				condition.setActionSet(Set.of(TaskAction.OUTPAINT2X,TaskAction.OUTPAINT2X))
						.setFinalPromptEn(parseData.getPrompt());
			}
			findAndFinishImageTask(condition, parseData.getPrompt(), message);
		}
	}

	public static void main(String[] args) {
		String s = "**a flying man** - Upscaled (4x) by <@426580409665716235> (fast)";
		ContentParseData parseData = ConvertUtils.parseContent(s, CONTENT_REGEX);
		System.out.println(parseData);
	}

}
