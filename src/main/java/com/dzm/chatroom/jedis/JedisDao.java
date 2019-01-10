package com.dzm.chatroom.jedis;

public interface JedisDao {

	Boolean exists(String key);
	
	Long del(String key);
	
	String set(String key, String value);
	
	String get(String key);
	
}