package com.dmm.middleware.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Mean
 * @date 2025/1/16 21:57
 * @description XRedisProperties
 */
@ConfigurationProperties("redis")
public class XRedisProperties {
	private String host;
	private int port;

	// todo 其余配置待完善

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
