package com.redis.counter;

public class RedisRunable implements Runnable{
	public static int counter = 1;
	private String key = "test";
	
	public void run() {
		RedisCounter redisCounter = RedisCounter.getInstance();
		
		redisCounter.increaseTimes(key);
		int times = redisCounter.getTimes(key);
		
		System.out.println(counter++ + ":" + times);
	}
}
