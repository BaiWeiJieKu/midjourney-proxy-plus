package com.github.novicezk.midjourney.loadbalancer;


import com.github.novicezk.midjourney.domain.DiscordAccount;
import com.github.novicezk.midjourney.result.Message;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import com.github.novicezk.midjourney.service.DiscordService;
import com.github.novicezk.midjourney.support.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * discord账号实例服务接口
 * @author qinfen
 * @date 2024/01/29
 */
public interface DiscordInstance extends DiscordService {

	/**
	 * 获取账号channelId
	 * @return {@link String}
	 */
	String getInstanceId();

	/**
	 * 获取discord账号
	 * @return {@link DiscordAccount}
	 */
	DiscordAccount account();

	/**
	 * 是否存活
	 * @return boolean
	 */
	boolean isAlive();

	/**
	 * 开启websocket连接
	 * @throws Exception
	 */
	void startWss() throws Exception;

	/**
	 * 获取运行中的任务列表
	 * @return {@link List}<{@link Task}>
	 */
	List<Task> getRunningTasks();

	/**
	 * 退出任务
	 * @param task
	 */
	void exitTask(Task task);

	/**
	 * 获取运行中的线程任务
	 * @return {@link Map}<{@link String}, {@link Future}<{@link ?}>>
	 */
	Map<String, Future<?>> getRunningFutures();

	/**
	 * 提交任务
	 * @param task
	 * @param discordSubmit
	 * @return {@link SubmitResultVO}
	 */
	SubmitResultVO submitTask(Task task, Callable<Message<Void>> discordSubmit);

}
