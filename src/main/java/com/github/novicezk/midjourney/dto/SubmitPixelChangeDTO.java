package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.TaskAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@ApiModel("像素变化任务提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitPixelChangeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "任务ID", required = true, example = "\"1320098173412546\"")
	private String taskId;

	@ApiModelProperty(value = "UPSAMPLE2X(像素放大2倍); UPSAMPLE4X(像素放大4倍); OUTPAINT15X(缩放1.5倍并补充细节生成四张图); OUTPAINT2X(缩放2倍并补充细节生成四张图)", required = true,
			allowableValues = "UPSAMPLE2X, UPSAMPLE4X, OUTPAINT15X, OUTPAINT2X", example = "UPSAMPLE2X")
	private TaskAction action;


}
