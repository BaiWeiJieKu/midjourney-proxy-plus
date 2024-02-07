package com.github.novicezk.midjourney.dto;

import com.github.novicezk.midjourney.enums.TaskAction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@ApiModel("方向延伸变化任务提交参数")
@EqualsAndHashCode(callSuper = true)
public class SubmitDirectionChangeDTO extends BaseSubmitDTO {

	@ApiModelProperty(value = "任务ID", required = true, example = "\"1320098173412546\"")
	private String taskId;

	@ApiModelProperty(value = "DIRECTION_UP(往上延伸生成四张图), DIRECTION_DOWN(往下延伸生成四张图), DIRECTION_LEFT(往左延伸生成四张图), DIRECTION_RIGHT(往右延伸生成四张图)", required = true,
			allowableValues = "DIRECTION_UP, DIRECTION_DOWN, DIRECTION_LEFT, DIRECTION_RIGHT", example = "DIRECTION_DOWN")
	private TaskAction action;


}
