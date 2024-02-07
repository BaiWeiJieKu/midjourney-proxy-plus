package com.github.novicezk.midjourney.wss.handle;

import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.ConvertUtils;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

/**
 * pan up,down,left,right
 * 完成(create): **a flying dog --ar 3:2** - Pan Left by <@426580409665716235> (fast)
 */
@Component
public class DirectionSuccessHandler extends MessageHandler {
	private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - Pan (.*?) by <@\\d+> \\((.*?)\\)";


	@Override
	public void handle(MessageType messageType, DataObject message) {
		String content = getMessageContent(message);
		ContentParseData parseData = ConvertUtils.parseContent(content, CONTENT_REGEX);
		if (MessageType.CREATE.equals(messageType) && parseData != null && hasImage(message)) {
			TaskCondition condition = new TaskCondition();
			if (content.contains("Pan Up by")) {
				condition.setActionSet(Set.of(TaskAction.DIRECTION_UP))
						.setFinalPromptEn(parseData.getPrompt());
			}
			if (content.contains("Pan Down by")) {
				condition.setActionSet(Set.of(TaskAction.DIRECTION_DOWN))
						.setFinalPromptEn(parseData.getPrompt());
			}
			if (content.contains("Pan Left by")) {
				condition.setActionSet(Set.of(TaskAction.DIRECTION_LEFT))
						.setFinalPromptEn(parseData.getPrompt());
			}
			if (content.contains("Pan Right by")) {
				condition.setActionSet(Set.of(TaskAction.DIRECTION_RIGHT))
						.setFinalPromptEn(parseData.getPrompt());
			}
			findAndFinishImageTask(condition, parseData.getPrompt(), message);
		}
	}

	public static void main(String[] args) {
		String s = "**a flying dog --ar 3:2** - Pan Left by <@426580409665716235> (fast)";
		ContentParseData parseData = ConvertUtils.parseContent(s, CONTENT_REGEX);
		System.out.println(parseData);
	}

}
