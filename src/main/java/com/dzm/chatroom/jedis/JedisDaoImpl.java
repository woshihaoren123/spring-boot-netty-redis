package com.dzm.chatroom.jedis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import redis.clients.jedis.JedisPool;

@Service
public class JedisDaoImpl implements JedisDao{
	
	@Autowired
	private JedisPool jedisClient;
	
	public Boolean exists(String key) {
		return jedisClient.getResource().exists(key);
	}

	public Long del(String key) {
		return jedisClient.getResource().del(key);
	}

	public String set(String key, String value) {
		return jedisClient.getResource().set(key, value);
	}

	public String get(String key) {
		return jedisClient.getResource().get(key);
	}

}
