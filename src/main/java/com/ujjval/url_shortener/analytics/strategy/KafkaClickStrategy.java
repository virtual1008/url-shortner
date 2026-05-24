package com.ujjval.url_shortener.analytics.strategy;

import com.ujjval.url_shortener.analytics.config.KafkaTopicConfig;
import com.ujjval.url_shortener.analytics.dto.ClickEventDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Primary
public class KafkaClickStrategy implements  ClickTrackingStrategy{
    private static final Logger log = LoggerFactory.getLogger(KafkaClickStrategy.class);

    private final KafkaTemplate<String,Object> kafkaTemplate;
    private final RedisClickStrategy fallbackStrategy;

    public KafkaClickStrategy(KafkaTemplate<String,Object> kafkaTemplate , RedisClickStrategy fallbackStrategy){
        this.kafkaTemplate = kafkaTemplate;
        this.fallbackStrategy = fallbackStrategy;
    }

    @Override
    @CircuitBreaker(name = "kafkaBreaker", fallbackMethod = "fallbackToRedis")
    public void trackClick(String shortCode, String ipAddress, String userAgent) {
        ClickEventDto event = new ClickEventDto(shortCode, ipAddress, LocalDateTime.now(), userAgent);

        kafkaTemplate.send(KafkaTopicConfig.RAW_CLICKS_TOPIC, shortCode, event);
        log.info("Publisher: Successfully sent click event to Kafka for: {}", shortCode);
    }

    public void fallbackToRedis(String shortCode, String clientIp,String userAgent, Throwable throwable) {
        log.error("Circuit breaker triggered! Kafka failed: {}", throwable.getMessage());
        fallbackStrategy.trackClick(shortCode, clientIp,userAgent);
    }
}
