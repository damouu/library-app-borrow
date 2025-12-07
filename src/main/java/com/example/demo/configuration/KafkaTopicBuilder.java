package com.example.demo.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicBuilder {

    @Bean
    public NewTopic borrowTopic() {
        return TopicBuilder.name("borrow_topic").build();
    }

    @Bean
    public NewTopic returnTopic() {
        return TopicBuilder.name("return_topic").build();
    }

}
