package com.ujjval.url_shortener.idgenerator.impl;

import com.ujjval.url_shortener.idgenerator.IdGenerationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisCounterIdGenerationStrategy  implements IdGenerationStrategy {
    private final StringRedisTemplate redisTemplate;

    private final String redisKey = "url:id:counter";

    @Override
    public long generateId() {
        Long id = redisTemplate.opsForValue().increment(redisKey);
        if(id==null){
            throw new RuntimeException("Redis ID generation failed");
        }
        return id;
    }
}
