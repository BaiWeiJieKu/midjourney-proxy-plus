package com.github.novicezk.midjourney.service;

import com.github.novicezk.midjourney.enums.BlendDimensions;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.support.Task;
import eu.maxschuster.dataurl.DataUrl;

import java.util.List;

public interface TaskService {

	SubmitResultVO submitImagine(Task task, List<DataUrl> dataUrls);

	SubmitResultVO submitUpscaleX(Task task, String targetMessageId, String targetMessageHash, int num, int messageFlags);
	SubmitResultVO submitOutpaintX(Task task, String targetMessageId, String targetMessageHash, int num, int messageFlags);

	SubmitResultVO submitUpscale(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags);

	SubmitResultVO submitVariation(Task task, String targetMessageId, String targetMessageHash, int index, int messageFlags);

	SubmitResultVO submitVariationLow(Task task, String targetMessageId, String targetMessageHash, int messageFlags);

	SubmitResultVO submitVariationHigh(Task task, String targetMessageId, String targetMessageHash, int messageFlags);

	SubmitResultVO submitReroll(Task task, String targetMessageId, String targetMessageHash, int messageFlags);

	SubmitResultVO submitDescribe(Task task, DataUrl dataUrl);

	SubmitResultVO submitBlend(Task task, List<DataUrl> dataUrls, BlendDimensions dimensions);

	SubmitResultVO submitDirection(String direction,Task task, String targetMessageId, String targetMessageHash, int messageFlags);
}