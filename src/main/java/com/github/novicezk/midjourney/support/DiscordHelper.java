package com.github.novicezk.midjourney.support;

import cn.hutool.core.text.CharSequenceUtil;
import com.github.novicezk.midjourney.ProxyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * discord配置类（URL地址）
 * @author qinfen
 * @date 2024/01/26
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DiscordHelper {
	private final ProxyProperties properties;
	/**
	 * DISCORD_SERVER_URL.
	 */
	public static final String DISCORD_SERVER_URL = "https://discord.com";
	/**
	 * DISCORD_CDN_URL.
	 */
	public static final String DISCORD_CDN_URL = "https://cdn.discordapp.com";
	/**
	 * DISCORD_WSS_URL.
	 */
	public static final String DISCORD_WSS_URL = "wss://gateway.discord.gg";
	/**
	 * DISCORD_UPLOAD_URL.
	 */
	public static final String DISCORD_UPLOAD_URL = "https://discord-attachments-uploads-prd.storage.googleapis.com";

	/**
	 * 获取discord服务URL，如果有反向代理，走反向代理地址，DISCORD_SERVER_URL
	 * @return {@link String}
	 */
	public String getServer() {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getServer())) {
			return DISCORD_SERVER_URL;
		}
		String serverUrl = this.properties.getNgDiscord().getServer();
		if (serverUrl.endsWith("/")) {
			serverUrl = serverUrl.substring(0, serverUrl.length() - 1);
		}
		return serverUrl;
	}

	/**
	 * 获取discord服务cdn地址，如果有反向代理，走反向代理地址，DISCORD_CDN_URL
	 * @return {@link String}
	 */
	public String getCdn() {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getCdn())) {
			return DISCORD_CDN_URL;
		}
		String cdnUrl = this.properties.getNgDiscord().getCdn();
		if (cdnUrl.endsWith("/")) {
			cdnUrl = cdnUrl.substring(0, cdnUrl.length() - 1);
		}
		return cdnUrl;
	}

	/**
	 * 获取discord服务websocket地址，如果有反向代理，走反向代理地址，DISCORD_WSS_URL
	 * @return {@link String}
	 */
	public String getWss() {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getWss())) {
			return DISCORD_WSS_URL;
		}
		String wssUrl = this.properties.getNgDiscord().getWss();
		if (wssUrl.endsWith("/")) {
			wssUrl = wssUrl.substring(0, wssUrl.length() - 1);
		}
		return wssUrl;
	}

	/**
	 * 获取discord服务上传图片地址，如果有反向代理，走反向代理地址，DISCORD_UPLOAD_URL
	 * @param uploadUrl 上传地址，没有反代直接返回此URL
	 * @return {@link String}
	 */
	public String getDiscordUploadUrl(String uploadUrl) {
		if (CharSequenceUtil.isBlank(this.properties.getNgDiscord().getUploadServer()) || CharSequenceUtil.isBlank(uploadUrl)) {
			return uploadUrl;
		}
		String uploadServer = this.properties.getNgDiscord().getUploadServer();
		if (uploadServer.endsWith("/")) {
			uploadServer = uploadServer.substring(0, uploadServer.length() - 1);
		}
		return uploadUrl.replaceFirst(DISCORD_UPLOAD_URL, uploadServer);
	}

	/**
	 * 通过cdn地址查询任务ID
	 * @param url
	 * @return {@link String}
	 */
	public String findTaskIdWithCdnUrl(String url) {
		if (!CharSequenceUtil.startWith(url, DISCORD_CDN_URL)) {
			return null;
		}
		int hashStartIndex = url.lastIndexOf("/");
		String taskId = CharSequenceUtil.subBefore(url.substring(hashStartIndex + 1), ".", true);
		if (CharSequenceUtil.length(taskId) == 16) {
			return taskId;
		}
		return null;
	}

	/**
	 * @param imageUrl
	 * @return {@link String}
	 */
	public String getMessageHash(String imageUrl) {
		if (CharSequenceUtil.isBlank(imageUrl)) {
			return null;
		}
		if (CharSequenceUtil.endWith(imageUrl, "_grid_0.webp")) {
			int hashStartIndex = imageUrl.lastIndexOf("/");
			if (hashStartIndex < 0) {
				return null;
			}
			return CharSequenceUtil.sub(imageUrl, hashStartIndex + 1, imageUrl.length() - "_grid_0.webp".length());
		}
		int hashStartIndex = imageUrl.lastIndexOf("_");
		if (hashStartIndex < 0) {
			return null;
		}
		return CharSequenceUtil.subBefore(imageUrl.substring(hashStartIndex + 1), ".", true);
	}

}
