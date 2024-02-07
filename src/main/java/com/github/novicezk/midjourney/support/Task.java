package com.github.novicezk.midjourney.support;

import com.github.novicezk.midjourney.domain.DomainObject;
import com.github.novicezk.midjourney.enums.TaskAction;
import com.github.novicezk.midjourney.enums.TaskStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serial;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel("任务")
@ToString(callSuper = true)
public class Task extends DomainObject {
	@Serial
	private static final long serialVersionUID = -674915748204390789L;

	/**
	 * 任务类型
	 */
	@ApiModelProperty("任务类型")
	private TaskAction action;
	/**
	 * 任务状态，默认NOT_START
	 */
	@ApiModelProperty("任务状态")
	private TaskStatus status = TaskStatus.NOT_START;

	/**
	 * 提示词
	 */
	@ApiModelProperty("提示词")
	private String prompt;
	/**
	 * 提示词-英文
	 */
	@ApiModelProperty("提示词-英文")
	private String promptEn;

	/**
	 * 任务描述
	 */
	@ApiModelProperty("任务描述")
	private String description;
	/**
	 * 自定义参数
	 */
	@ApiModelProperty("自定义参数")
	private String state;

	/**
	 * 提交时间
	 */
	@ApiModelProperty("提交时间")
	private Long submitTime;
	/**
	 * 开始执行时间
	 */
	@ApiModelProperty("开始执行时间")
	private Long startTime;
	/**
	 * 结束时间
	 */
	@ApiModelProperty("结束时间")
	private Long finishTime;

	/**
	 * 图片url
	 */
	@ApiModelProperty("图片url")
	private String imageUrl;

	/**
	 * progress
	 */
	@ApiModelProperty("任务进度")
	private String progress;
	/**
	 * 失败原因
	 */
	@ApiModelProperty("失败原因")
	private String failReason;

	/**
	 * 开始任务
	 */
	public void start() {
		this.startTime = System.currentTimeMillis();
		this.status = TaskStatus.SUBMITTED;
		this.progress = "0%";
	}

	/**
	 * 任务成功
	 */
	public void success() {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.SUCCESS;
		this.progress = "100%";
	}

	/**
	 * 任务失败
	 * @param reason
	 */
	public void fail(String reason) {
		this.finishTime = System.currentTimeMillis();
		this.status = TaskStatus.FAILURE;
		this.failReason = reason;
		this.progress = "";
	}
}
