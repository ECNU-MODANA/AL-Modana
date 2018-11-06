package ecnu.modana.util;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;



public class RedisUtil {

	private static String ADDR = "47.101.58.16";

    private static int PORT = 6379;
    //密码
    private static String AUTH = "31592653";
    //连接实例的最大连接数
    private static int MAX_ACTIVE = 100;

    //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
    private static int MAX_IDLE = 200;
    
    //等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException
    private static int MAX_WAIT = 10000;
    
    //连接超时的时间　　
    private static int TIMEOUT = 3000;

    // 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
    private static boolean TEST_ON_BORROW = true;

    private static ShardedJedisPool  jedisPool = null;

    /**
     * 初始化Redis连接池
     */

    static {

    	redis.clients.jedis.JedisPoolConfig jedisPoolConfig = new redis.clients.jedis.JedisPoolConfig();
    		jedisPoolConfig.setMaxTotal(MAX_ACTIVE);
    		jedisPoolConfig.setMaxIdle(MAX_IDLE);
    		jedisPoolConfig.setMaxWaitMillis(MAX_WAIT);
    		jedisPoolConfig.setTestOnBorrow(TEST_ON_BORROW);
    		JedisShardInfo jedisShardInfo = new JedisShardInfo("47.101.58.16",6379);
            jedisShardInfo.setPassword(AUTH);
            List<JedisShardInfo> list = new ArrayList<>();
            list.add(jedisShardInfo);
            jedisPool = new ShardedJedisPool(jedisPoolConfig, list);

    }

    /**
     * 获取Jedis实例
     */

    public synchronized static ShardedJedis getJedis() {

        try {

            if (jedisPool != null) {
            	ShardedJedis resource = jedisPool.getResource();
                return resource;
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /***
     * 
     * 释放资源
     */
    
    public static void returnResource(final ShardedJedis jedis) {
            if(jedis != null) {
                jedisPool.returnResource(jedis);
            }
        
    }
    
    
}