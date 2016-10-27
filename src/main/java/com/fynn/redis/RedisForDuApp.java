package com.fynn.redis;

import redis.clients.jedis.Jedis;


public class RedisForDuApp implements Redis{
	private Jedis jedis;
	private static String databaseName = "tqyCvrULbNJWVSAPzXIq"; //数据库名称
    private static String host = "redis.duapp.com";
    private static String portStr = "80";
    private static int port = Integer.parseInt(portStr);
    private static String username = "cc538df7723642d0a2bfff9c30ff825a"; //用户AK
    private static String password = "505c77771694471797d3d3848d07d6d9"; //密码SK

	protected RedisForDuApp(){
        jedis = new Jedis(host, port);
        jedis.connect();
        jedis.auth(username + "-" + password + "-" + databaseName);
	}
	
	public Jedis getJedis() {
		return this.jedis;
	}

	public void returnJedis(Jedis jedis) {
		// bae引擎暂时不支持jedisPool
	}

	public void destroyJedisPool() {
		// bae引擎暂时不支持jedisPool
	}
 }