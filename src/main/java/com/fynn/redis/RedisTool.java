package com.fynn.redis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fynn.tools.util.Utils;
import com.fynn.util.Mode;

import redis.clients.jedis.Jedis;

public class RedisTool {
	private static RedisTool instance;
	private static BlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000);
	private static Redis redis;
	private static Properties prop;
	static{
		prop = new Properties();
		InputStream in = RedisForLocal.class.getResourceAsStream("/redis.properties");
		try {
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * author:fynn.liu
	 * time:2016-9-26下午4:34:09 
	 * description:得到RedisCounter实例,并启动消息读取线程
	 */
	public static RedisTool getInstance() {
		synchronized (RedisTool.class) {
			if (instance == null) {
				instance = new RedisTool();
				
				if(Mode.LOCAL.equals(prop.getProperty("mode"))){
					redis = new RedisForLocal();
				}else if(Mode.DUAPP.equals(prop.getProperty("mode"))){
					redis = new RedisForDuApp();
				}
				
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
			
			return instance;
		}
	}
	
	/**
	 * 
	 * author:fynn.liu
	 * time:2016-9-26下午6:11:42
	 * description:销毁redis资源
	 */
	public void destory(){
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
		return Utils.stringToInt(result);
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
	
	public synchronized boolean setKeyValue(String key,String value){
		return queue.offer(key + "-" + value);
	}
	
	public synchronized String getValue(String key){
		Jedis jedis = redis.getJedis();
		String result = jedis.get(key);
		
		//一定要回收jedis资源,不然很快就用完,出现异常
		redis.returnJedis(jedis);
		return result;
	}
	
	public synchronized Long addList(String key,String value){
		Jedis jedis = redis.getJedis();
		Long result = jedis.lpush(key, value);
		
		//一定要回收jedis资源,不然很快就用完,出现异常
		redis.returnJedis(jedis);
		return result;
	}
	
	public boolean exsitKey(String key){
		Jedis jedis = redis.getJedis();
		Boolean result = jedis.exists(key);
		
		//一定要回收jedis资源,不然很快就用完,出现异常
		redis.returnJedis(jedis);
		return result;
	}
	
}
