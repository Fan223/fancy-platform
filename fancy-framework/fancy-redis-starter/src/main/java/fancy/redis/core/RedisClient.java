package fancy.redis.core;

import org.redisson.api.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 客户端操作类.
 *
 * @author Fan
 * @since 2025/3/6 16:19
 */
public class RedisClient {

    private final RedissonClient redissonClient;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisClient(RedissonClient redissonClient, RedisTemplate<String, Object> redisTemplate) {
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 判断 Key 是否存在.
     *
     * @param key {@link String}
     * @return {@link Boolean}
     * @author GreyFable
     * @since 2025/3/6 16:40
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除 Key.
     *
     * @param key {@link String}
     * @return {@link Boolean}
     * @author GreyFable
     * @since 2025/3/6 16:41
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 获取 Key 的过期时间, 默认单位秒.
     *
     * @param key {@link String}
     * @return {@link Long}
     * @author GreyFable
     * @since 2025/3/6 17:02
     */
    public Long getExpire(String key) {
        return getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 获取 Key 的过期时间, 指定时间单位.
     *
     * @param key  {@link String}
     * @param unit {@link TimeUnit}
     * @return {@link Long}
     * @author GreyFable
     * @since 2025/3/7 8:38
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 设置 Key 的过期时间, 默认单位秒.
     *
     * @param key     {@link String}
     * @param timeout 过期时间
     * @return {@link Boolean}
     * @author GreyFable
     * @since 2025/3/7 8:40
     */
    public Boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置 Key 的过期时间, 指定时间单位.
     *
     * @param key     {@link String}
     * @param timeout 过期时间
     * @param unit    {@link TimeUnit}
     * @return {@link Boolean}
     * @author GreyFable
     * @since 2025/3/6 17:02
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * String, 设置 Key-Value.
     *
     * @param key   {@link String}
     * @param value {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:43
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * String, 设置 Key-Value, 同时设置过期时间, 默认单位秒.
     *
     * @param key     {@link String}
     * @param value   {@link Object}
     * @param timeout 过期时间
     * @author GreyFable
     * @since 2025/3/6 16:44
     */
    public void set(String key, Object value, long timeout) {
        set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * String, 设置 Key-Value, 同时设置过期时间, 指定时间单位.
     *
     * @param key     {@link String}
     * @param value   {@link Object}
     * @param timeout 过期时间
     * @param unit    {@link TimeUnit}
     * @author GreyFable
     * @since 2025/3/6 16:46
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * String, 获取 Key 的值.
     *
     * @param key {@link String}
     * @return {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:43
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Hash, 设置 Key-HashKey-Value, 或者为 Key Field Value.
     *
     * @param key     {@link String}
     * @param hashKey {@link String}
     * @param value   {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:48
     */
    public void hSet(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * Hash, 获取 Key-HashKey 的值.
     *
     * @param key     {@link String}
     * @param hashKey {@link String}
     * @return {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:47
     */
    public Object hGet(String key, String hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * Hash, 获取 Key 的值.
     *
     * @param key {@link String}
     * @return {@link Map}
     * @author GreyFable
     * @since 2025/3/7 11:54
     */
    public Map<Object, Object> hEntries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Hash, 增加 Key-HashKey 指定值.
     *
     * @param key       {@link String}
     * @param hashKey   {@link String}
     * @param increment 值
     * @author GreyFable
     * @since 2025/3/7 11:44
     */
    public void hIncrement(String key, String hashKey, long increment) {
        redisTemplate.opsForHash().increment(key, hashKey, increment);
    }

    /**
     * Hash, 删除 Key-HashKey.
     *
     * @param key      {@link String}
     * @param hashKeys {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:48
     */
    public void hDel(String key, Object... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }

    /**
     * List, 设置 Key-Value, 在头部添加元素.
     *
     * @param key   {@link String}
     * @param value {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:51
     */
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * List, 设置 Key-Value, 在尾部添加元素.
     *
     * @param key   {@link String}
     * @param value {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:56
     */
    public void rPush(String key, Object value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * List, 获取 Key 的值, 移除并获取头部的元素.
     *
     * @param key {@link String}
     * @return {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:56
     */
    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * List, 获取 Key 的值, 移除并获取尾部的元素.
     *
     * @param key {@link String}
     * @return {@link Object}
     * @author GreyFable
     * @since 2025/3/6 16:57
     */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * Set, 判断 Key 是否包含指定值.
     *
     * @param key   {@link String}
     * @param value {@link Object}
     * @return {@link Boolean}
     * @author GreyFable
     * @since 2025/3/6 16:58
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * Set, 设置 Key-Value, 添加元素.
     *
     * @param key    {@link String}
     * @param values 元素
     * @author GreyFable
     * @since 2025/3/6 16:58
     */
    public void sAdd(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Set, 获取 Key 的值.
     *
     * @param key {@link String}
     * @return {@link Set<Object>}
     * @author GreyFable
     * @since 2025/3/6 16:57
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }


    /**
     * Set, 在 Key 中删除值并返回删除数量.
     *
     * @param key    {@link String}
     * @param values 元素
     * @return {@link Long}
     * @author GreyFable
     * @since 2025/3/7 9:28
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * Sorted set, 设置 Key-Value, 添加元素.
     *
     * @param key   {@link String}
     * @param value {@link Object}
     * @param score 评分
     * @author GreyFable
     * @since 2025/3/6 17:00
     */
    public void zAdd(String key, Object value, double score) {
        redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Sorted set, 从 Key 中获取 start 和 end 之间的元素, score 从低到高.
     *
     * @param key   {@link String}
     * @param start 起始
     * @param end   结束
     * @return {@link Set<Object>}
     * @author GreyFable
     * @since 2025/3/6 16:59
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * Sorted set, 获取 Key 中指定元素的排名, score 从低到高.
     *
     * @param key   {@link String}
     * @param value {@link Object}
     * @return {@link Long}
     * @author GreyFable
     * @since 2025/3/6 17:01
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取 {@link RLock}.
     *
     * @param lockKey {@link String}
     * @return {@link RLock}
     * @author GreyFable
     * @since 2025/3/7 10:46
     */
    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    /**
     * 获取分布式锁, 阻塞等待, 不会自动释放.
     *
     * @param lockKey {@link String}
     * @author GreyFable
     * @since 2025/3/7 10:13
     */
    public void lock(String lockKey) {
        RLock lock = getLock(lockKey);
        lock.lock();
    }

    /**
     * 尝试获取分布式锁, 不等待, 不会自动释放.
     *
     * @param lockKey {@link String}
     * @return {@code  boolean}
     * @author GreyFable
     * @since 2025/3/7 10:49
     */
    public boolean tryLock(String lockKey) {
        RLock lock = getLock(lockKey);
        return lock.tryLock();
    }

    /**
     * 尝试获取分布式锁, 可以等待一段时间, 不会自动释放.
     *
     * @param lockKey {@link String}
     * @param time    超时时间
     * @param unit    {@link TimeUnit}
     * @return {@link boolean}
     * @author GreyFable
     * @since 2025/3/7 10:51
     */
    public boolean tryLock(String lockKey, long time, TimeUnit unit) {
        RLock lock = getLock(lockKey);
        try {
            return lock.tryLock(time, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 尝试获取分布式锁, 可以等待一段时间, 到期后自动释放.
     *
     * @param lockKey   {@link String}
     * @param waitTime  等待时间
     * @param leaseTime 持有时间
     * @param unit      {@link TimeUnit}
     * @return {@code boolean}
     * @author GreyFable
     * @since 2025/3/6 17:05
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放分布式锁.
     *
     * @param lockKey {@link String}
     * @author GreyFable
     * @since 2025/3/6 17:06
     */
    public void unlock(String lockKey) {
        RLock lock = getLock(lockKey);
        if (lock.isLocked() && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 获取原子类 {@link RAtomicLong}.
     *
     * @param name {@link String}
     * @return {@link RAtomicLong}
     * @author GreyFable
     * @since 2025/3/7 10:58
     */
    public RAtomicLong getAtomicLong(String name) {
        return redissonClient.getAtomicLong(name);
    }

    /**
     * 获取原子类 {@link RAtomicDouble}.
     *
     * @param name {@link String}
     * @return {@link RAtomicDouble}
     * @author GreyFable
     * @since 2025/3/7 10:59
     */
    public RAtomicDouble getAtomicDouble(String name) {
        return redissonClient.getAtomicDouble(name);
    }

    /**
     * 获取布隆过滤器 {@link RBloomFilter}.
     *
     * @param name {@link String}
     * @return {@link RBloomFilter}
     * @author GreyFable
     * @since 2025/3/7 11:00
     */
    public <V> RBloomFilter<V> getBloomFilter(String name) {
        return redissonClient.getBloomFilter(name);
    }
}
