package com.cloud.sysconf.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Redis Client
 * 需要用到的服务需在启动类添加扫描
 */
@Component
public class RedisClient {

    @Value("${jedis.pool.config.maxTotal}")int maxTotal;
    @Value("${jedis.pool.config.maxIdle}")int maxIdle;
    @Value("${jedis.pool.config.maxWaitMillis}")int maxWaitMillis;
    @Value("${jedis.pool.host}") String host;
    @Value("${jedis.pool.port}")int port;
    @Value("${jedis.pool.password}")String password;

    public JedisPoolConfig jedisPoolConfig () {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWaitMillis);
        return config;
    }

    @Bean
    public JedisPool jedisPool(){
        return new JedisPool(jedisPoolConfig(), host, port,1000,password);
    }

    @Autowired
    private JedisPool jedisPool;

    protected Jedis getJedis() {
        Jedis jedis = null;
        try {

            jedis = jedisPool.getResource();
        } catch (JedisException e) {
            if (jedis != null) {
                jedis.close();
                // jedisPool.returnBrokenResource(jedis);
            }

        }
        return jedis;
    }
    /**
     *
     * <p>功能描述：注销</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:40:40</p>
     *
     * @param jedis
     * @param isBroken
     */
    protected  void release(Jedis jedis, boolean isBroken) {
        if (jedis != null ) {
            jedis.close();
        }
    }

    /**
     *
     * <p>功能描述：返回哈希表 key 中，所有的域和值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:40:52</p>
     *
     * @param db
     * @param key
     * @return
     */
    public  Map<String, String> GetHincrByJedis(int db, String key) {

        Jedis jedis = getJedis();
        jedis.select(db);
        Map<String, String> map = null;
        boolean isBroken = false;
        try {

            map = jedis.hgetAll(key);

        } catch (Exception e) {
            isBroken = true;
        } finally {

            release(jedis, isBroken);
        }
        return map;
    }

    /**
     *
     * <p>功能描述：将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:41:18</p>
     *
     * @param db
     * @param key
     * @param value
     */
    public  void SetOperateJedis(int db, String key, String value) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.sadd(key, value);

        } catch (Exception e) {
            isBroken = true;
        } finally {

            release(jedis, isBroken);
        }
    }

    /**
     *
     * <p>功能描述：删除一个元素 在set集合中</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:41:18</p>
     *
     * @param db
     * @param key
     * @param value
     */
    public  void DelOperateJedis(int db, String key, String value) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.srem(key, value);

        } catch (Exception e) {
            isBroken = true;
        } finally {

            release(jedis, isBroken);
        }
    }


    /**
     *
     * <p>功能描述：同时将多个 field-value (域-值)对设置到哈希表 key 中</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:41:53</p>
     *
     * @param db
     * @param key
     * @param hash
     */
    public  void SetHmsetJedis(int db, String key, Map<String, String> hash) {
        String string;
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            string = jedis.hmset(key, hash);

        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：判断有序列表是否有该KEY无新增有删除</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:56:50</p>
     *
     * @param db
     * @param key
     * @param member
     * @param score
     * @return
     */
    public  int SetZaddJedis(int db, String key, String member, Double score) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            Double s = jedis.zscore(key, member);
            if (s == null) {
                jedis.zadd(key, score, member);
                return 1;
            } else {
                jedis.zrem(key, member);
                return -1;
            }

        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return 0;
    }

    /**
     *
     * <p>功能描述：新增一个或多个member到有序key里</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:56:50</p>
     *
     * @param db
     * @param key
     * @param scoreMembers
     * @return
     */
    public  void SetZaddJedisMap(int db, String key,Map<String,Double> scoreMembers) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            jedis.zadd(key, scoreMembers);


        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }


    /**
     *
     * <p>功能描述：新增一个或多个member到有序key里</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:56:50</p>
     *
     * @param db
     * @param key
     * @return
     */
    public  long GetJedisMapSize(int db, String key) {

        Jedis jedis = getJedis();
        jedis.select(db);
        long size=0;
        boolean isBroken = false;
        try {

            size= jedis.zcard(key);


        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return size;

    }




    /**
     *
     * <p>功能描述：删除一个member</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:56:50</p>
     *
     * @param db
     * @param key
     * @param members
     * @return
     */
    public  void DelZaddJedisMap(int db, String key,String members) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            jedis.zrem(key, members);


        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：返回有序集 key 中，指定区间内的成员</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:56:29</p>
     *
     * @param db
     * @param key
     * @param start
     * @param end
     * @return
     */
    public  Set<String> GetZrevrangeJedis(int db, String key, long start, long end) {
        Set<String> setlist = null;
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            setlist = jedis.zrevrange(key, start, end);
        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return setlist;
    }


    /**
     *
     * <p>功能描述：返回有序集 key 中，指定区间内的成员</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:56:29</p>
     *
     * @param db
     * @param key
     * @param start
     * @param end
     * @return
     */
    public  Set<byte[]> GetZrevrangeJedis2(int db, byte[] key, long start, long end) {
        Set<byte[]> setlist = null;
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            setlist = jedis.zrevrange(key, start, end);
        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return setlist;
    }

    /**
     *
     * <p>功能描述：返回有序集 key 中，成员 member 的 score 值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:43:42</p>
     *
     * @param db
     * @param key
     * @param member
     * @return
     */
    public  Double GetZscoreJedis(int db, String key, String member) {
        Double s = null;
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            s = jedis.zscore(key, member);
        } catch (Exception e) {

            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return s;
    }

    /**
     *
     * <p>功能描述：将哈希表 key 中的域 field 的值设为 value </p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:34:04</p>
     *
     * @param db
     * @param key
     * @param field
     * @param value
     */
    public  void SetHsetJedis(int db, String key, String field, String value) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.hset(key, field, value);

        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：批量将哈希表 key 中的域 field 的值设为 value </p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:34:04</p>
     *
     * @param db
     * @param key
     * @param map
     */
    public  void SetHsetJedis(int db, String key,Map<String, String> map) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.hmset(key, map);

        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：将哈希表 key 中的域 field 的值设为 value </p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:34:04</p>
     *
     * @param db
     * @param key
     * @param field
     * @param value
     */
    public  void SetHsetJedis2(int db, String key, String field, String value) {

        SetHsetJedis(db, key, field, value);

    }

    /**
     *
     * <p>功能描述：查找所有符合给定模式 pattern 的 key </p>
     * KEYS * 匹配数据库中所有 key 。
     KEYS h?llo 匹配 hello ， hallo 和 hxllo 等。
     KEYS h*llo 匹配 hllo 和 heeeeello 等。
     KEYS h[ae]llo 匹配 hello 和 hallo ，但不匹配 hillo 。
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:33:16</p>
     *
     * @param db
     * @param pattern
     * @return
     */
    public  Set<String> GetWhereKeys(int db, String pattern) {
        Jedis jedis = getJedis();
        jedis.select(db);
        Set<String> set = null;
        boolean isBroken = false;
        try {
            set = jedis.keys(pattern);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return set;
    }

    /**
     *
     * <p>功能描述：判断 member 元素是否集合 key 的成员</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:32:35</p>
     *
     * @param db
     * @param key
     * @param value
     * @return
     */
    public Boolean Getsismember(int db, String key, String value) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        Boolean istrue = false;
        try {

            istrue = jedis.sismember(key, value);
        } catch (Exception ex) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return istrue;
    }

    /**
     *
     * <p>功能描述：返回哈希表 key 中给定域 field 的值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:32:18</p>
     *
     * @param db
     * @param key
     * @param filed
     * @return
     */
    public  String Gethget(int db, String key, String filed) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        String valueString = null;
        try {
            valueString = jedis.hget(key, filed);

        } catch (Exception ex) {
            isBroken = true;

        } finally {
            release(jedis, isBroken);
        }

        return valueString;
    }

    /**
     *
     * <p>功能描述：返回哈希表 key 中，一个或多个给定域的值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:31:57</p>
     *
     * @param db
     * @param key
     * @param fields
     * @return
     */
    public  List<String> Gethgets(int db, String key, String... fields) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        List<String> valueString = null;
        try {
            valueString = jedis.hmget(key, fields);
        } catch (Exception e) {
            isBroken = true;

        } finally {
            release(jedis, isBroken);
        }

        return valueString;
    }

    /**
     *
     * <p>功能描述：返回哈希表 key 中，所有的域和值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:31:08</p>
     *
     * @param db
     * @param key
     * @return
     */
    public  Map<String, String> Gethgetall(int db, String key) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        Map<String, String> map = null;
        try {
            map = jedis.hgetAll(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

        return map;
    }

    /**
     *
     * <p>功能描述：返回哈希表 key 中域的数量</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:30:39</p>
     *
     * @param db
     * @param key
     * @return
     */
    public  long Gethlen(int db, String key) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        long len = 0;
        try {
            len = jedis.hlen(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

        return len;
    }

    /**
     *
     * <p>功能描述：将值 value 关联到 key ，并将 key 的生存时间设为 seconds (以秒为单位)</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:30:00</p>
     *
     * @param db
     * @param key
     * @param value
     * @param seconds
     */
    public  void SetShardedJedis(int db, String key, String value, int seconds) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.setex(key, seconds, value);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：将字符串值 value 关联到 key</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:29:29</p>
     *
     * @param db
     * @param key
     * @param value
     */
    public  void SetShardedJedis(int db, String key, String value) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.set(key, value);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：为哈希表 key 中的域 field 的值加上增量 increment 无返回值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:27:29</p>
     *
     * @param db
     * @param key
     * @param field
     * @param value
     */
    public  void SetHincrbyJedis(int db, String key, String field, long value) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：为哈希表 key 中的域 field 的值加上增量 increment 有返回值</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:27:08</p>
     *
     * @param db
     * @param key
     * @param field
     * @param
     * @return
     */
    public  long SetHincrbyJedis2(int db, String key, String field, long value) {

        long i = 0;
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            i = jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return i;

    }

    /**
     *
     * <p>功能描述：将 key 中储存的数字值增一</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年1月9日 上午10:26:36</p>
     *
     * @param db
     * @param key
     */
    public  void incradd(int db, String key) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            jedis.incr(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     * 删除
     * @param db
     * @param key
     * @return
     */
    public  void del(int db, String key) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            jedis.del(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *  返回 key 所关联的字符串值
     * @param db
     *
     * @param key
     * @return
     */
    public  String get(int db, String key) {
        String value = null;

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            value = jedis.get(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return value;
    }
    /**
     *  返回 key 所关联的字符串值
     * @param db
     *
     * @param key
     * @param  value
     * @return
     */
    public  void set(int db, String key,String value) {
        Jedis jedis = getJedis();
        jedis.select(db);
        jedis.set(key,value);
    }

    /**
     *  返回 key 所关联的字符串值
     * @param db
     *
     * @param key
     * @param  value
     * @return
     */
    public  void setTtl(int db, String key,String value,Integer ttl) {
        Jedis jedis = getJedis();
        jedis.select(db);
        jedis.set(key,value);
    }

    /**
     *对一个列表进行修剪(trim)
     * @param db
     *
     * @param key
     * @return
     */
    public  String Ltrim(int db, String key, int start, int end) {
        String value = null;

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {

            value = jedis.ltrim(key, start, end);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return value;
    }

    public  void SetExpire(int db, String key, int seconds) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.expire(key, seconds);

        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：塞入队列第一位</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年2月9日 下午2:21:42</p>
     *
     * @param db
     * @param key
     * @param value
     */
    public  void lpush(int db,String key,String value)
    {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.lpush(key, value);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     *
     * <p>功能描述：批量塞入队列第一位</p>
     * <p>创建人：linmin</p>
     * <p>创建日期：2017年07月27日 10:41:50</p>
     *
     * @param db
     * @param key
     * @param array
     */
    public  void lpushArray(int db, String key, String[] array)
    {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.lpush(key, array);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
    }

    /**
     *
     * <p>功能描述：取出队列</p>
     * <p>创建人：sky</p>
     * <p>创建日期：2017年2月9日 下午2:22:09</p>
     *
     * @param db
     * @param key
     */
    public  String rpoplpush(int db,String key)
    {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        String value = "";
        try {
            value = jedis.rpop(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }
        return value;
    }
    /**
     *
     * <p>功能描述：删除key中某个field</p>
     * <p>创建人：guopengjie</p>
     * <p>创建日期：2017年2月9日 下午2:32:41</p>
     *
     * @param db
     * @param key
     * @param field
     */
    public  void deleteJedis(int db, String key, String field) {
        long l = 0;
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            l = jedis.hdel(key, field);
            System.out.println(l);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }
    /**
     *
     * <p>功能描述：获取key中所有值集合</p>
     * <p>创建人：guopengjie</p>
     * <p>创建日期：2017年2月14日 下午4:45:34</p>
     *
     * @param db
     * @param key
     * @return
     */
    public  Set<String> getSmembers(int db, String key) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        Set<String> set = null;
        try {
            set = jedis.smembers(key);
        } catch (Exception e) {
            isBroken = true;
            System.out.println(e);
        } finally {
            release(jedis, isBroken);
        }

        return set;
    }
    /**
     *
     * <p>功能描述：获取zset成员数量</p>
     * <p>创建人：guopengjie</p>
     * <p>创建日期：2017年2月23日 上午10:54:00</p>
     *
     * @param db
     * @param key
     * @return
     */
    public  long GetZLen(int db, String key) {
        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        long len = 0;
        try {
            len = jedis.zcard(key);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

        return len;
    }
    /**
     *
     * <p>功能描述：为 key 中的值加上增量 increment 无返回值</p>
     * <p>创建人：guopengjie</p>
     * <p>创建日期：2017年4月1日 上午10:27:29</p>
     *
     * @param db
     * @param key
     * @param value
     */
    public  void SetincrbyJedis(int db, String key, long value) {

        Jedis jedis = getJedis();
        jedis.select(db);
        boolean isBroken = false;
        try {
            jedis.incrBy(key, value);
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
        }

    }

    /**
     * 分页获取redis中的数据
     * @param db
     * @param patternKey
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<String> findKeysForPage(int db, String patternKey, int pageNum, int pageSize) {
        Jedis jedis = getJedis();
        jedis.select(db);
        SortingParams sortingParams = new SortingParams();
        sortingParams.desc();
        jedis.sort(patternKey+"*", sortingParams, "createTime");
        boolean isBroken = false;
        List<String> result = new ArrayList<>(pageSize);
        try {

            long maxsize = jedis.dbSize();
            // 调用jedis对象的方法，方法名称和redis的命令一致。
            ScanParams scanParams = new ScanParams();
            scanParams.match(patternKey+"*");
            scanParams.count(pageSize);

            // scan(curso,params) cursor 表示开始遍历的游标   params 是ScanParams 对象，此对象可以设置 每次返回的数量，以及遍历时的正则表达式
            // 需要注意的是，对元素的模式匹配工作是在命令从数据集中取出元素之后，向客户端返回元素之前的这段时间内进行的，
            //  所以如果被迭代的数据集中只有少量元素和模式相匹配，那么迭代命令或许会在多次执行中都不返回任何元素。

            long c = maxsize - pageNum * pageSize;
            while(c >= 0) {
                ScanResult<String> scan = jedis.scan(c+"", scanParams);

                long i = 1;
                for (int j = scan.getResult().size()-1; j>=0; j--){
                    if (i < pageSize) {
                        result.add(scan.getResult().get(j));
                        i++;
                    } else {
                        break;
                    }
                }
                if(result.size() >= pageSize || c<pageSize){
                    c = -1;
                }else{
                    c = c-pageSize;
                }
            }
        } catch (Exception e) {
            isBroken = true;
        } finally {
            release(jedis, isBroken);
            return result;
        }
    }

}
