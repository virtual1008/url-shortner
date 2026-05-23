package com.ujjval.url_shortener.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BloomFilterConfig {

    @Bean
    public RBloomFilter<String> shortCodeBloomFilter(RedissonClient redissonClient){
        RBloomFilter<String> bloomFilter = redissonClient.getBloomFilter("linkscale:bloom:shortcodes");
        bloomFilter.tryInit(10_000_000L,0.01);
        return bloomFilter;
    }
}
