package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.support.Task;

/**
 * 任务通知服务
 * @author qinfen
 * @date 2024/01/29
 */
public interface NotifyService {

	/**
	 * 通知任务变更
	 * @param task 任务
	 */
	void notifyTaskChange(Task task);

}
