package com.github.novicezk.midjourney.service;


import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.support.TaskCondition;

import java.util.List;

/**
 * 任务存储服务，支持内存memory和Redis两种存储方式
 * @author qinfen
 * @date 2024/01/26
 */
public interface TaskStoreService {

	/**
	 * 创建任务
	 * @param task
	 */
	void save(Task task);

	/**
	 * 删除任务
	 * @param id
	 */
	void delete(String id);

	/**
	 * 获取任务
	 * @param id
	 * @return {@link Task}
	 */
	Task get(String id);

	/**
	 * 查询任务列表
	 * @return {@link List}<{@link Task}>
	 */
	List<Task> list();

	List<Task> list(TaskCondition condition);

	Task findOne(TaskCondition condition);

}
