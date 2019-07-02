package com.imooc.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 基于Redis分布锁
 */
@Slf4j
public class ReidsLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key 商品号
     * @param value 当前时间 + 超时时间
     * @return
     */
    public boolean lock(String key, String value) {
        // setIfAbsent 如果不存在就保存并返回true，否则返回false
        if (redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }
        String currentValue = redisTemplate.opsForValue().get(key);
        // 如果锁过期了
        if (StringUtils.isNoneBlank(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            // 获取上个锁时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (StringUtils.isNoneBlank(oldValue) && oldValue.equals(currentValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unLock(String key, String value) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (StringUtils.isNoneBlank(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("Redis分布式锁解锁异常：{}", e);
        }
    }
}
