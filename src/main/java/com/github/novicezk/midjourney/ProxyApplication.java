package com.github.novicezk.midjourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import spring.config.BeanConfig;
import spring.config.LettuceConfig;
import spring.config.RedisConfig;
import spring.config.WebMvcConfig;

@EnableScheduling
@SpringBootApplication
@Import({BeanConfig.class, WebMvcConfig.class, LettuceConfig.class, RedisConfig.class})
public class ProxyApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}

}
