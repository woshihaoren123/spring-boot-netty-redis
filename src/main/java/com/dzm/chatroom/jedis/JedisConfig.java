package com.dzm.chatroom.jedis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class JedisConfig extends CachingConfigurerSupport{

	@Value("${redis.host}")
	private String host;
	
	@Value("${redis.port}")
	private String port;

	@Value("${redis.time-out}")
	private int timeOut;

	@Bean
	public JedisPool redisPoolFactory() {
		JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		jedisPoolConfig.setMaxIdle(8);
		jedisPoolConfig.setMaxWaitMillis(-1);
		jedisPoolConfig.setMaxTotal(200);
		jedisPoolConfig.setMinIdle(0);
		JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, Integer.valueOf(port));
		return jedisPool;
	}
}
