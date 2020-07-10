package com.jarvis.cache.lock;

import redis.clients.jedis.JedisCluster;

/**
 * 分布式锁，基于Redis集群模式
 */
public class JedisClusterLock extends AbstractRedisLock {

    private JedisCluster jedisCluster;

    public JedisClusterLock(JedisCluster jedisCluster) {
        this.jedisCluster = jedisCluster;
    }

    @Override
    protected boolean setnx(String key, String val, int expire) {
        return OK.equalsIgnoreCase(jedisCluster.set(key, val, NX, EX, expire));
    }

    @Override
    protected void del(String key) {
        this.jedisCluster.del(key);
    }

}
