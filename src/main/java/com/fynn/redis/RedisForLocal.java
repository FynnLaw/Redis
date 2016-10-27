package com.fynn.redis;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.fynn.tools.util.Utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public final class RedisForLocal implements Redis{
	private static Logger logger = Logger.getLogger(RedisForLocal.class);
	private JedisPool jedisPool;
	private String host;
	private int port;
	private String auth;
	private int maxActive;
	private int maxIdle;//控制一个pool最多有多少个状态为空闲的jedis实例,默认值也是8
	private int maxWait;//等待可用连接的最大时间,单位毫秒,默认值为-1,表示永不超时;如果超过等待时间,则直接抛出JedisConnectionException
	private int timeout;
	private boolean testOnBorrow;//在borrow一个jedis实例时,是否提前进行validate操作;如果为true,则得到的jedis实例均是可用的

	protected RedisForLocal(){
		InputStream in = null;
		try {
	        Properties prop = new Properties();
	        in = RedisForLocal.class.getResourceAsStream("/redis.properties");
            prop.load(in);
            
            this.host = Utils.nullOrBlank(prop.getProperty("host")) ? "localhost" : prop.getProperty("host");
            this.port = Utils.nullOrBlank(prop.getProperty("port")) ? 6379 : new Integer(prop.getProperty("port"));
            this.auth = Utils.nullOrBlank(prop.getProperty("auth")) ? null : prop.getProperty("auth");
            this.maxActive = Utils.nullOrBlank(prop.getProperty("maxActive")) ? 1024 : new Integer(prop.getProperty("maxActive"));
            this.maxWait = Utils.nullOrBlank(prop.getProperty("maxWait")) ? 10000 : new Integer(prop.getProperty("maxWait"));
            this.timeout = Utils.nullOrBlank(prop.getProperty("timeout")) ? 10000 : new Integer(prop.getProperty("timeout"));
            this.maxIdle = Utils.nullOrBlank(prop.getProperty("maxIdle")) ? 1024 : new Integer(prop.getProperty("maxIdle"));
            this.testOnBorrow = Utils.nullOrBlank(prop.getProperty("testOnBorrow")) ? true : new Boolean(prop.getProperty("testOnBorrow"));
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
        	try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
				logger.error("文件流未成功关闭");
			}
        }
	}
	
	/**
	 * 获取Jedis实例
	 * 
	 * @return
	 */
	public synchronized Jedis getJedis() {
		try {
			if (jedisPool != null) {
				return jedisPool.getResource();
			} else {
				JedisPoolConfig config = new JedisPoolConfig();
				config.setMaxTotal(this.maxActive);
				config.setMaxIdle(this.maxIdle);
				config.setMaxWaitMillis(this.maxWait);
				config.setTestOnBorrow(this.testOnBorrow);
				
				jedisPool = new JedisPool(config, this.host,this.port,this.timeout, this.auth);
				
				return jedisPool.getResource();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void returnJedis(Jedis jedis){
		jedisPool.returnResource(jedis);
	}
	
	public void destroyJedisPool(){
		jedisPool.destroy();
	}

}