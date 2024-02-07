package spring.config;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReflectUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import com.github.novicezk.midjourney.loadbalancer.rule.IRule;
import com.github.novicezk.midjourney.service.NotifyService;
import com.github.novicezk.midjourney.service.TaskStoreService;
import com.github.novicezk.midjourney.service.TranslateService;
import com.github.novicezk.midjourney.service.store.InMemoryTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.store.RedisTaskStoreServiceImpl;
import com.github.novicezk.midjourney.service.translate.BaiduTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.GPTTranslateServiceImpl;
import com.github.novicezk.midjourney.service.translate.NoTranslateServiceImpl;
import com.github.novicezk.midjourney.support.DiscordAccountHelper;
import com.github.novicezk.midjourney.support.DiscordHelper;
import com.github.novicezk.midjourney.support.Task;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class BeanConfig {
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	private ProxyProperties properties;

	/**
	 * 翻译服务，支持百度，GPT
	 * @return {@link TranslateService}
	 */
	@Bean
	TranslateService translateService() {
		return switch (this.properties.getTranslateWay()) {
			case BAIDU -> new BaiduTranslateServiceImpl(this.properties.getBaiduTranslate());
			case GPT -> new GPTTranslateServiceImpl(this.properties);
			default -> new NoTranslateServiceImpl();
		};
	}

	/**
	 * 任务存储服务，默认使用内存存储
	 * @param redisConnectionFactory
	 * @return {@link TaskStoreService}
	 */
	@Bean
	TaskStoreService taskStoreService(RedisConnectionFactory redisConnectionFactory) {
		ProxyProperties.TaskStore.Type type = this.properties.getTaskStore().getType();
		Duration timeout = this.properties.getTaskStore().getTimeout();
		return switch (type) {
			case IN_MEMORY -> new InMemoryTaskStoreServiceImpl(timeout);
			case REDIS -> new RedisTaskStoreServiceImpl(timeout, taskRedisTemplate(redisConnectionFactory));
		};
	}

	/**
	 * taskRedisTemplate
	 * @param redisConnectionFactory
	 * @return {@link RedisTemplate}<{@link String}, {@link Task}>
	 */
	@Bean
	RedisTemplate<String, Task> taskRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Task> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setHashKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Task.class));
		return redisTemplate;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * 负载均衡配置，用于discord账号选择
	 * @return {@link IRule}
	 */
	@Bean
	public IRule loadBalancerRule() {
		String ruleClassName = IRule.class.getPackageName() + "." + this.properties.getAccountChooseRule();
		return ReflectUtil.newInstance(ruleClassName);
	}

	/**
	 * 消息处理
	 * @return {@link List}<{@link MessageHandler}>
	 */
	@Bean
	List<MessageHandler> messageHandlers() {
		return this.applicationContext.getBeansOfType(MessageHandler.class).values().stream().toList();
	}

	/**
	 * @param discordHelper
	 * @param taskStoreService
	 * @param notifyService
	 * @return {@link DiscordAccountHelper}
	 * @throws IOException
	 */
	@Bean
	DiscordAccountHelper discordAccountHelper(DiscordHelper discordHelper, TaskStoreService taskStoreService, NotifyService notifyService) throws IOException {
		//组装参数
		var resources = this.applicationContext.getResources("classpath:api-params/*.json");
		Map<String, String> paramsMap = new HashMap<>();
		for (var resource : resources) {
			String filename = resource.getFilename();
			String params = IoUtil.readUtf8(resource.getInputStream());
			paramsMap.put(filename.substring(0, filename.length() - 5), params);
		}
		return new DiscordAccountHelper(discordHelper, this.properties, restTemplate(), taskStoreService, notifyService, messageHandlers(), paramsMap);
	}


}
