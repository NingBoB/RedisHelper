package com.dmm.middleware.test.service;

import com.dmm.middleware.redis.annotation.XRedis;

/**
 * @author Mean
 * @date 2025/1/17 15:09
 * @description 测试接口，由代理实现
 */
@XRedis
public interface IRedisService {

	String get(String key);

	void set(String key, String val);

}

