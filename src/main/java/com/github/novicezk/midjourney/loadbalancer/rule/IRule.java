package com.github.novicezk.midjourney.loadbalancer.rule;

import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;

import java.util.List;

/**
 * discord账号池账号选择规则
 * @author qinfen
 * @date 2024/01/29
 */
public interface IRule {

	/**
	 * discord账号池账号选择
	 * @param instances
	 * @return {@link DiscordInstance}
	 */
	DiscordInstance choose(List<DiscordInstance> instances);
}
