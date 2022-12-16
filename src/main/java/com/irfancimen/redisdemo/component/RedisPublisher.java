package com.irfancimen.redisdemo.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile(value = "basic")
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.topic.name}")
    private String topic;

    public void publish(Object message) {
        redisTemplate.convertAndSend(topic, message);
    }

    @Scheduled(fixedRate = 10000)
    public void sendMessage() {
        String message = "Test message for all subscriber " + UUID.randomUUID();
        this.publish(message);
        log.info("Message is published: {} ", message);
    }
}
