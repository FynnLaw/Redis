package com.redis.counter;

public class RedisRunable implements Runnable{
	public static int counter = 1;
	private String key = "test";
	
	public void run() {
		RedisTool redisCounter = RedisTool.getInstance();
		
		redisCounter.increaseTimes(key);
		int times = redisCounter.getTimes(key);
		
		System.out.println(counter++ + ":" + times);
	}
}
