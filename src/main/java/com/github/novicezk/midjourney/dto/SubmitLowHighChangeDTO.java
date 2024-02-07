package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.TaskAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@ApiModel("细微强烈变化任务提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitLowHighChangeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "任务ID", required = true, example = "\"1320098173412546\"")
	private String taskId;

	@ApiModelProperty(value = "VARIATION_LOW(细微变化生成四张图), VARIATION_HIGH(强烈变化生成四张图)", required = true,
			allowableValues = "VARIATION_LOW, VARIATION_HIGH", example = "VARIATION_LOW")
	private TaskAction action;


}
