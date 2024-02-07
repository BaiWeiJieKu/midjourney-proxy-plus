package com.github.novicezk.midjourney.controller;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.dto.*;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import com.github.novicezk.midjourney.enums.TranslateWay;
import com.github.novicezk.midjourney.exception.BannedPromptException;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.TaskService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;
import com.github.novicezk.midjourney.util.BannedPromptUtils;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.util.MimeTypeUtils;
import com.github.novicezk.midjourney.util.SnowFlake;
import com.github.novicezk.midjourney.util.TaskChangeParams;
import eu.maxschuster.dataurl.DataUrl;
import eu.maxschuster.dataurl.DataUrlSerializer;
import eu.maxschuster.dataurl.IDataUrlSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api(tags = "任务提交")
@RestController
@RequestMapping("/submit")
@RequiredArgsConstructor
@Slf4j
public class SubmitController {
	private final TranslateService translateService;
	private final TaskStoreService taskStoreService;
	private final ProxyProperties properties;
	private final TaskService taskService;

	@ApiOperation(value = "提交Imagine任务")
	@PostMapping("/imagine")
	public SubmitResultVO imagine(@RequestBody SubmitImagineDTO imagineDTO) {
		String prompt = imagineDTO.getPrompt();
		if (CharSequenceUtil.isBlank(prompt)) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "prompt不能为空");
		}
		prompt = prompt.trim();
		Task task = newTask(imagineDTO);
		task.setAction(TaskAction.IMAGINE);
		task.setPrompt(prompt);
		//翻译提示词
		String promptEn = translatePrompt(prompt);
		try {
			//提示词敏感词判断
			BannedPromptUtils.checkBanned(promptEn);
		} catch (BannedPromptException e) {
			return SubmitResultVO.fail(ReturnCode.BANNED_PROMPT, "可能包含敏感词")
					.setProperty("promptEn", promptEn).setProperty("bannedWord", e.getMessage());
		}
		List<String> base64Array = Optional.ofNullable(imagineDTO.getBase64Array()).orElse(new ArrayList<>());
		if (CharSequenceUtil.isNotBlank(imagineDTO.getBase64())) {
			base64Array.add(imagineDTO.getBase64());
		}
		List<DataUrl> dataUrls;
		try {
			dataUrls = ConvertUtils.convertBase64Array(base64Array);
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64格式错误");
		}
		task.setPromptEn(promptEn);
		task.setDescription("/imagine " + prompt);
		log.info("SubmitController.imagine--->task:{}",task);
		return this.taskService.submitImagine(task, dataUrls);
	}

	@ApiOperation(value = "绘图变化-simple")
	@PostMapping("/simple-change")
	public SubmitResultVO simpleChange(@RequestBody SubmitSimpleChangeDTO simpleChangeDTO) {
		TaskChangeParams changeParams = ConvertUtils.convertChangeParams(simpleChangeDTO.getContent());
		if (changeParams == null) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "content参数错误");
		}
		SubmitChangeDTO changeDTO = new SubmitChangeDTO();
		changeDTO.setAction(changeParams.getAction());
		changeDTO.setTaskId(changeParams.getId());
		changeDTO.setIndex(changeParams.getIndex());
		changeDTO.setState(simpleChangeDTO.getState());
		changeDTO.setNotifyHook(simpleChangeDTO.getNotifyHook());
		return change(changeDTO);
	}

	@ApiOperation(value = "绘图变化")
	@PostMapping("/change")
	public SubmitResultVO change(@RequestBody SubmitChangeDTO changeDTO) {
		if (CharSequenceUtil.isBlank(changeDTO.getTaskId())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "taskId不能为空");
		}
		if (!Set.of(TaskAction.UPSCALE, TaskAction.VARIATION, TaskAction.REROLL).contains(changeDTO.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "action参数错误");
		}
		String description = "/up " + changeDTO.getTaskId();
		if (TaskAction.REROLL.equals(changeDTO.getAction())) {
			description += " R";
		} else {
			description += " " + changeDTO.getAction().name().charAt(0) + changeDTO.getIndex();
		}
		if (TaskAction.UPSCALE.equals(changeDTO.getAction())) {
			TaskCondition condition = new TaskCondition().setDescription(description);
			Task existTask = this.taskStoreService.findOne(condition);
			if (existTask != null) {
				return SubmitResultVO.of(ReturnCode.EXISTED, "任务已存在", existTask.getId())
						.setProperty("status", existTask.getStatus())
						.setProperty("imageUrl", existTask.getImageUrl());
			}
		}
		Task targetTask = this.taskStoreService.get(changeDTO.getTaskId());
		if (targetTask == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "关联任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务状态错误");
		}
		if (!Set.of(TaskAction.IMAGINE, TaskAction.VARIATION, TaskAction.REROLL, TaskAction.BLEND).contains(targetTask.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务不允许执行变化");
		}
		Task task = newTask(changeDTO);
		task.setAction(changeDTO.getAction());
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, targetTask.getProperty(Constants.TASK_PROPERTY_FINAL_PROMPT));
		task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_MESSAGE_ID));
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID));
		task.setDescription(description);
		int messageFlags = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_FLAGS);
		String messageId = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_ID);
		String messageHash = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_HASH);
		if (TaskAction.UPSCALE.equals(changeDTO.getAction())) {
			return this.taskService.submitUpscale(task, messageId, messageHash, changeDTO.getIndex(), messageFlags);
		} else if (TaskAction.VARIATION.equals(changeDTO.getAction())) {
			return this.taskService.submitVariation(task, messageId, messageHash, changeDTO.getIndex(), messageFlags);
		} else {
			return this.taskService.submitReroll(task, messageId, messageHash, messageFlags);
		}
	}

	/**
	 * 针对单个图片放大或缩小
	 * @param changeDTO
	 * @return {@link SubmitResultVO}
	 */
	@ApiOperation(value = "放大缩小变化")
	@PostMapping("/pixelChange")
	public SubmitResultVO pixelChange(@RequestBody SubmitPixelChangeDTO changeDTO) {
		if (CharSequenceUtil.isBlank(changeDTO.getTaskId())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "taskId不能为空");
		}
		if (!Set.of(TaskAction.UPSAMPLE2X, TaskAction.UPSAMPLE4X, TaskAction.OUTPAINT15X,TaskAction.OUTPAINT2X).contains(changeDTO.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "action参数错误");
		}
		String description = "/up " + changeDTO.getTaskId() + " " + changeDTO.getAction().name();

		//放大任务
		if (TaskAction.UPSAMPLE2X.equals(changeDTO.getAction())
				|| TaskAction.UPSAMPLE4X.equals(changeDTO.getAction())
				|| TaskAction.OUTPAINT15X.equals(changeDTO.getAction())
				|| TaskAction.OUTPAINT2X.equals(changeDTO.getAction())) {
			TaskCondition condition = new TaskCondition()
					.setDescription(description)
					.setStatusSet(Set.of(TaskStatus.SUBMITTED,TaskStatus.IN_PROGRESS));
			Task existTask = this.taskStoreService.findOne(condition);
			if (existTask != null) {
				return SubmitResultVO.of(ReturnCode.EXISTED, "任务已存在", existTask.getId())
						.setProperty("status", existTask.getStatus())
						.setProperty("imageUrl", existTask.getImageUrl());
			}
		}
		Task targetTask = this.taskStoreService.get(changeDTO.getTaskId());
		if (targetTask == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "关联任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务状态错误");
		}
		if (!Set.of(TaskAction.UPSCALE).contains(targetTask.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务不允许执行变化");
		}
		Task task = newTask(changeDTO);
		task.setAction(changeDTO.getAction());
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, targetTask.getProperty(Constants.TASK_PROPERTY_FINAL_PROMPT));
		task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_MESSAGE_ID));
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID));
		task.setDescription(description);
		int messageFlags = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_FLAGS);
		String messageId = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_ID);
		String messageHash = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_HASH);
		if (TaskAction.UPSAMPLE2X.equals(changeDTO.getAction())) {
			return this.taskService.submitUpscaleX(task, messageId, messageHash, 2, messageFlags);
		} else if (TaskAction.UPSAMPLE4X.equals(changeDTO.getAction())) {
			return this.taskService.submitUpscaleX(task, messageId, messageHash, 4, messageFlags);
		} else if (TaskAction.OUTPAINT15X.equals(changeDTO.getAction())) {
			return this.taskService.submitOutpaintX(task, messageId, messageHash, 15, messageFlags);
		}else {
			return this.taskService.submitOutpaintX(task, messageId, messageHash, 2, messageFlags);
		}
	}


	@ApiOperation(value = "细微变化和强烈变化")
	@PostMapping("/lowAndHighChange")
	public SubmitResultVO lowAndHighChange(@RequestBody SubmitLowHighChangeDTO changeDTO) {
		if (CharSequenceUtil.isBlank(changeDTO.getTaskId())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "taskId不能为空");
		}
		if (!Set.of(TaskAction.VARIATION_LOW, TaskAction.VARIATION_HIGH).contains(changeDTO.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "action参数错误");
		}
		String description = "/up " + changeDTO.getTaskId() + " " + changeDTO.getAction().name();

		//校验任务是否已存在
		if (TaskAction.VARIATION_LOW.equals(changeDTO.getAction())
				|| TaskAction.VARIATION_HIGH.equals(changeDTO.getAction())) {
			TaskCondition condition = new TaskCondition()
					.setDescription(description)
					.setStatusSet(Set.of(TaskStatus.SUBMITTED,TaskStatus.IN_PROGRESS));
			Task existTask = this.taskStoreService.findOne(condition);
			if (existTask != null) {
				return SubmitResultVO.of(ReturnCode.EXISTED, "任务已存在", existTask.getId())
						.setProperty("status", existTask.getStatus())
						.setProperty("imageUrl", existTask.getImageUrl());
			}
		}
		Task targetTask = this.taskStoreService.get(changeDTO.getTaskId());
		if (targetTask == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "关联任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务状态错误");
		}
		if (!Set.of(TaskAction.UPSCALE).contains(targetTask.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务不允许执行变化");
		}
		Task task = newTask(changeDTO);
		task.setAction(changeDTO.getAction());
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, targetTask.getProperty(Constants.TASK_PROPERTY_FINAL_PROMPT));
		task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_MESSAGE_ID));
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID));
		task.setDescription(description);
		int messageFlags = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_FLAGS);
		String messageId = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_ID);
		String messageHash = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_HASH);
		if (TaskAction.VARIATION_LOW.equals(changeDTO.getAction())) {
			return this.taskService.submitVariationLow(task, messageId, messageHash, messageFlags);
		} else {
			return this.taskService.submitVariationHigh(task, messageId, messageHash, messageFlags);
		}
	}

	@ApiOperation(value = "方向延伸变化")
	@PostMapping("/directionChange")
	public SubmitResultVO directionChange(@RequestBody SubmitDirectionChangeDTO changeDTO) {
		if (CharSequenceUtil.isBlank(changeDTO.getTaskId())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "taskId不能为空");
		}
		if (!Set.of(TaskAction.DIRECTION_UP, TaskAction.DIRECTION_DOWN,TaskAction.DIRECTION_LEFT,TaskAction.DIRECTION_RIGHT).contains(changeDTO.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "action参数错误");
		}
		String description = "/up " + changeDTO.getTaskId() + " " + changeDTO.getAction().name();

		//校验任务是否已存在
		if (TaskAction.DIRECTION_UP.equals(changeDTO.getAction())
				|| TaskAction.DIRECTION_DOWN.equals(changeDTO.getAction())
				|| TaskAction.DIRECTION_LEFT.equals(changeDTO.getAction())
				|| TaskAction.DIRECTION_RIGHT.equals(changeDTO.getAction())) {
			TaskCondition condition = new TaskCondition()
					.setDescription(description)
					.setStatusSet(Set.of(TaskStatus.SUBMITTED,TaskStatus.IN_PROGRESS));
			Task existTask = this.taskStoreService.findOne(condition);
			if (existTask != null) {
				return SubmitResultVO.of(ReturnCode.EXISTED, "任务已存在", existTask.getId())
						.setProperty("status", existTask.getStatus())
						.setProperty("imageUrl", existTask.getImageUrl());
			}
		}
		Task targetTask = this.taskStoreService.get(changeDTO.getTaskId());
		if (targetTask == null) {
			return SubmitResultVO.fail(ReturnCode.NOT_FOUND, "关联任务不存在或已失效");
		}
		if (!TaskStatus.SUCCESS.equals(targetTask.getStatus())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务状态错误");
		}
		if (!Set.of(TaskAction.UPSCALE).contains(targetTask.getAction())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "关联任务不允许执行变化");
		}
		Task task = newTask(changeDTO);
		task.setAction(changeDTO.getAction());
		task.setPrompt(targetTask.getPrompt());
		task.setPromptEn(targetTask.getPromptEn());
		task.setProperty(Constants.TASK_PROPERTY_FINAL_PROMPT, targetTask.getProperty(Constants.TASK_PROPERTY_FINAL_PROMPT));
		task.setProperty(Constants.TASK_PROPERTY_PROGRESS_MESSAGE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_MESSAGE_ID));
		task.setProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID, targetTask.getProperty(Constants.TASK_PROPERTY_DISCORD_INSTANCE_ID));
		task.setDescription(description);
		int messageFlags = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_FLAGS);
		String messageId = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_ID);
		String messageHash = targetTask.getPropertyGeneric(Constants.TASK_PROPERTY_MESSAGE_HASH);
		if (TaskAction.DIRECTION_UP.equals(changeDTO.getAction())) {
			return this.taskService.submitDirection("up",task, messageId, messageHash, messageFlags);
		} else if (TaskAction.DIRECTION_DOWN.equals(changeDTO.getAction())) {
			return this.taskService.submitDirection("down",task, messageId, messageHash, messageFlags);
		} else if (TaskAction.DIRECTION_LEFT.equals(changeDTO.getAction())) {
			return this.taskService.submitDirection("left",task, messageId, messageHash, messageFlags);
		}else {
			return this.taskService.submitDirection("right",task, messageId, messageHash, messageFlags);
		}
	}

	@ApiOperation(value = "提交Describe任务")
	@PostMapping("/describe")
	public SubmitResultVO describe(@RequestBody SubmitDescribeDTO describeDTO) {
		if (CharSequenceUtil.isBlank(describeDTO.getBase64())) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64不能为空");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		DataUrl dataUrl;
		try {
			dataUrl = serializer.unserialize(describeDTO.getBase64());
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64格式错误");
		}
		Task task = newTask(describeDTO);
		task.setAction(TaskAction.DESCRIBE);
		String taskFileName = task.getId() + "." + MimeTypeUtils.guessFileSuffix(dataUrl.getMimeType());
		task.setDescription("/describe " + taskFileName);
		return this.taskService.submitDescribe(task, dataUrl);
	}

	@ApiOperation(value = "提交Blend任务")
	@PostMapping("/blend")
	public SubmitResultVO blend(@RequestBody SubmitBlendDTO blendDTO) {
		List<String> base64Array = blendDTO.getBase64Array();
		if (base64Array == null || base64Array.size() < 2 || base64Array.size() > 5) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64List参数错误");
		}
		if (blendDTO.getDimensions() == null) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "dimensions参数错误");
		}
		IDataUrlSerializer serializer = new DataUrlSerializer();
		List<DataUrl> dataUrlList = new ArrayList<>();
		try {
			for (String base64 : base64Array) {
				DataUrl dataUrl = serializer.unserialize(base64);
				dataUrlList.add(dataUrl);
			}
		} catch (MalformedURLException e) {
			return SubmitResultVO.fail(ReturnCode.VALIDATION_ERROR, "base64格式错误");
		}
		Task task = newTask(blendDTO);
		task.setAction(TaskAction.BLEND);
		task.setDescription("/blend " + task.getId() + " " + dataUrlList.size());
		return this.taskService.submitBlend(task, dataUrlList, blendDTO.getDimensions());
	}

	private Task newTask(BaseSubmitDTO base) {
		Task task = new Task();
		task.setId(System.currentTimeMillis() + RandomUtil.randomNumbers(3));
		task.setSubmitTime(System.currentTimeMillis());
		task.setState(base.getState());
		String notifyHook = CharSequenceUtil.isBlank(base.getNotifyHook()) ? this.properties.getNotifyHook() : base.getNotifyHook();
		task.setProperty(Constants.TASK_PROPERTY_NOTIFY_HOOK, notifyHook);
		task.setProperty(Constants.TASK_PROPERTY_NONCE, SnowFlake.INSTANCE.nextId());
		return task;
	}

	private String translatePrompt(String prompt) {
		if (TranslateWay.NULL.equals(this.properties.getTranslateWay()) || CharSequenceUtil.isBlank(prompt)) {
			return prompt;
		}
		List<String> imageUrls = new ArrayList<>();
		Matcher imageMatcher = Pattern.compile("https?://[a-z0-9-_:@&?=+,.!/~*'%$]+\\x20+", Pattern.CASE_INSENSITIVE).matcher(prompt);
		while (imageMatcher.find()) {
			imageUrls.add(imageMatcher.group(0));
		}
		String paramStr = "";
		Matcher paramMatcher = Pattern.compile("\\x20+-{1,2}[a-z]+.*$", Pattern.CASE_INSENSITIVE).matcher(prompt);
		if (paramMatcher.find()) {
			paramStr = paramMatcher.group(0);
		}
		String imageStr = CharSequenceUtil.join("", imageUrls);
		String text = prompt.substring(imageStr.length(), prompt.length() - paramStr.length());
		if (CharSequenceUtil.isNotBlank(text)) {
			text = this.translateService.translateToEnglish(text).trim();
		}
		return imageStr + text + paramStr;
	}

}
