package com.ujjval.url_shortener.config;
import com.ujjval.url_shortener.idgenerator.IdGenerationStrategy;
import com.ujjval.url_shortener.idgenerator.impl.SnowflakeIdGenerationStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class AppConfig {
    @Bean
    @Primary
    public IdGenerationStrategy idGenerationStrategy(@Value("${application.snowflake.node-id}") long nodeId) {
        return new SnowflakeIdGenerationStrategy(nodeId);
    }

}