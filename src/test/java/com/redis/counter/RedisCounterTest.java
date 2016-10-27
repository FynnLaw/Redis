package com.redis.counter;

import org.junit.Test;

public class RedisCounterTest {
	private int testUserNumber = 1000;
	@Test
	public void test() throws InterruptedException {
		int j = 0;
		while(j < testUserNumber){
			RedisRunable runnable = new RedisRunable();
			new Thread(runnable,"consumer").start();
			j++;
		}
		
		Thread.sleep(6000);
		System.out.println("模拟" + j + "个用户同时访问！");
	}
}
