package com.irfancimen.redisdemo.component;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
@RequiredArgsConstructor
public class RedisOperation {

    private final RedisTemplate<String, Object> redisTemplate;

    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setList(String key, Object value){
        try {
            redisTemplate.opsForList().leftPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return  false;
        }
    }

    public Object lRangeAll(String key) {
        return key == null ? null : redisTemplate.opsForList().range(key, 0 , -1);
    }
    
    private <T> Class converterExtractor(Type target) {
        if (target != null && Void.class != target) {
            return (Class) target;
        }
        return null;
    }
}
