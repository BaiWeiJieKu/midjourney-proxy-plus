package com.github.novicezk.midjourney.support;


import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.Constants;
import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * api权限拦截器
 * @author qinfen
 * @date 2024/01/29
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAuthorizeInterceptor implements HandlerInterceptor {
	private final ProxyProperties properties;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		log.info("api权限拦截器========start==============");
		if (CharSequenceUtil.isBlank(this.properties.getApiSecret())) {
			log.info("api权限拦截器，没有配置apiSecret，放行");
			return true;
		}
		String apiSecret = request.getHeader(Constants.API_SECRET_HEADER_NAME);
		boolean authorized = CharSequenceUtil.equals(apiSecret, this.properties.getApiSecret());
		if (!authorized) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
		return authorized;
	}

}
