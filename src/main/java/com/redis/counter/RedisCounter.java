package com.redis.counter;

import java.util.Calendar;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import redis.clients.jedis.Jedis;

public class RedisCounter {
	private static RedisCounter instance;
	private static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000);
	private static Redis redis;
	
	/**
	 * author:fynn.liu
	 * time:2016-9-26下午4:34:09
	 * description:得到RedisCounter实例,并启动消息读取线程
	 */
	public static RedisCounter getInstance() {
		synchronized (RedisCounter.class) {
			if (instance == null) {
				instance = new RedisCounter();
				redis = new Redis();
				
				//启动读取queue消息线程
				ExecutorService executor = Executors.newSingleThreadExecutor();
				executor.execute(new Runnable() {
					
					public void run() {
						try {
							while(true){
								// 如果队列为空，会阻塞当前线程
								String string = queue.take();
								String[] str = string.split("-");

								String key = str[0];
								String value = str[1];
								
								if (value.equals("null") || value.equals("")) {
									redis.getJedis().incr(key);
								} else if(value.equals("expire")){
									//不做任何处理
								}else{
									redis.getJedis().set(key, String.valueOf(value));
								}
								
								if(str.length == 3){//设置key的超时时间
									int expireTime = new Integer(str[2]);
									redis.getJedis().expire(key, expireTime);
								}
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				});
			}
		}
		return instance;
	}
	
	/**
	 * 
	 * author:fynn.liu
	 * time:2016-9-26下午6:11:42
	 * description:销毁redis资源
	 */
	public static void destory(){
		redis.destroyJedisPool();
	}
	
	/**
	 * 根据key查询次数
	 * 
	 * @param key
	 * @return
	 */
	public int getTimes(String key) {
		Jedis jedis = redis.getJedis();
		String result = jedis.get(key);
		
		//一定要回收jedis资源,不然很快就用完,出现异常
		redis.returnJedis(jedis);
		return Redis.stringToInt(result);
	}

	/**
	 * 次数加一
	 * @param key
	 */
	public synchronized boolean increaseTimes(String key) {
		return queue.offer(key + "-null");
	}
	
	/**
	 * 
	 * author:fynn.liu
	 * time:2016-10-8下午3:29:34
	 * description:次数加一,每天24:00后key失效
	 */
	public synchronized boolean increaseDayTimes(String key) {
		Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    long secondTime = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
	    
	    return increaseTimesWithExpireTime(key, secondTime);
	}
	
	/**
	 * 
	 * author:fynn.liu
	 * time:2016-10-8下午3:44:00
	 * description:次数加一,每周最后一天24:00后key失效
	 */
	public synchronized boolean increaseWeekTimes(String key) {
		Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.DAY_OF_WEEK, cal.get(Calendar.DAY_OF_WEEK) + 1);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    long secondTime = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
	    
		return increaseTimesWithExpireTime(key, secondTime);
	}
	
	/**
	 * 
	 * author:fynn.liu
	 * time:2016-10-8下午3:30:28
	 * description:次数加一,每月最后一天24:00后key失效
	 */
	public synchronized boolean increaseMonthTimes(String key) {
		Calendar cal = Calendar.getInstance();
	    cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
	    cal.set(Calendar.DAY_OF_MONTH, 1);
	    cal.set(Calendar.HOUR_OF_DAY, 0);
	    cal.set(Calendar.MINUTE, 0);
	    cal.set(Calendar.SECOND, 0);
	    cal.set(Calendar.MILLISECOND, 0);
	    long secondTime = (cal.getTimeInMillis() - System.currentTimeMillis()) / 1000;
	    
		return increaseTimesWithExpireTime(key, secondTime);
	}
	
	/**
	 * 
	 * author:fynn.liu
	 * time:2016-10-8下午3:31:46
	 * description:次数加一,secondTime秒后key失效
	 */
	public synchronized boolean increaseTimesWithExpireTime(String key,long secondTime) {
		return queue.offer(key + "-null-" + secondTime);
	}
	
	/**
	 * 设置次数
	 * @param key
	 * @param value
	 */
	public synchronized boolean setTimes(String key,int value) {
		return queue.offer(key + "-" + value);
	}

	/**
	 * 
	 * author:fynn.liu
	 * time:2016-10-8下午4:11:38
	 * description:设置特定key的超时时间
	 */
	public synchronized boolean setKeyExpireTime(String key,int seconds){
		return queue.offer(key + "-expire-" + seconds);
	}
	
	/**
	 * 重置次数
	 * @param key
	 */
	public synchronized boolean resetTimes(String key) {
		return setTimes(key,0);
	}
	
}
