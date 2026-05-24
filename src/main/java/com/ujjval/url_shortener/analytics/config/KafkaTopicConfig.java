package com.ujjval.url_shortener.analytics.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    public static final String RAW_CLICKS_TOPIC = "raw-clicks";

    @Bean
    public NewTopic rawClicksTopic(){
        return TopicBuilder.name(RAW_CLICKS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
