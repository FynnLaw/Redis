package com.fynn.redis;

import redis.clients.jedis.Jedis;

public interface Redis {
	//get a jedis object
	Jedis getJedis();
	
	//return jedis to the jedisPool
	void returnJedis(Jedis jedis);
	
	//destroy the jedisPool 
	void destroyJedisPool();
}
