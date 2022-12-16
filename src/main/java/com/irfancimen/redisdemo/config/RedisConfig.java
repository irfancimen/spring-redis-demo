package com.irfancimen.redisdemo.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.irfancimen.redisdemo.component.RedisSubscriber;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;

    @Value("${spring.cache.redis.host:localhost}")
    private String host;

    @Value("${spring.cache.redis.port:6379}")
    private int port;

    @Value("${redis.topic.name:test}")
    private String topic;

    @Value("${spring.redis.cluster.nodes:localhost:6379}")
    private String clusterNodes;
    @Value("${spring.redis.cluster.max-redirects:1}")
    private int maxRedirects;

    @Value("${spring.redis.lettuce.cluster.refresh.period:1}")
    private int period;

    @Value("${spring.redis.timeout:1}")
    private int timeout;

    @Value("${spring.redis.sentinel.master:master}")
    private String master;

    @Value("${spring.redis.cluster.nodes: null}")
    private List<String> sentinelNodes;

    @Bean(name = "lettuceConnectionFactory")
    @Profile(value = "basic")
    public LettuceConnectionFactory lettuceBasicConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean(name = "lettuceConnectionFactory")
    @Profile(value = "cluster")
    public LettuceConnectionFactory lettuceClusterConnectionFactory() {
        String[] nodes = clusterNodes.split(",");
        List<RedisNode> nodeList = new ArrayList<>();

        for (String node: nodes) {
            String[] ipPort = node.split(":");
            RedisNode redisNode = new RedisNode(ipPort[0], Integer.parseInt(ipPort[1]));
            nodeList.add(redisNode);
        }

        RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration();
        redisClusterConfiguration.setClusterNodes(nodeList);
        redisClusterConfiguration.setMaxRedirects(maxRedirects);

        ClusterTopologyRefreshOptions clusterTopologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofSeconds(period))
                .enableAllAdaptiveRefreshTriggers()
                .build();

        ClusterClientOptions clusterClientOptions = ClusterClientOptions.builder()
                .timeoutOptions(TimeoutOptions.enabled(Duration.ofSeconds(period)))
                .topologyRefreshOptions(clusterTopologyRefreshOptions)
                .build();

        LettucePoolingClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(timeout))
                .readFrom(ReadFrom.REPLICA_PREFERRED)
                .clientOptions(clusterClientOptions)
                .build();

        return new LettuceConnectionFactory(redisClusterConfiguration, clientConfiguration);
    }

    @Bean(name = "lettuceConnectionFactory")
    @Profile(value = "sentinel")
    public LettuceConnectionFactory lettuceSentinelConnectionFactory() {
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.master(master);
        redisProperties.getSentinel().getNodes().forEach(sentinel -> {
            String[] props = sentinel.split(":");
            redisSentinelConfiguration.sentinel(props[0], Integer.parseInt(props[1]));
        });
        return new LettuceConnectionFactory(redisSentinelConfiguration);
    }

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        Jackson2JsonRedisSerializer<Object> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        jsonRedisSerializer.setObjectMapper(objectMapper);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
    @Bean
    @Profile(value = "basic && !cluster !sentinel")
    public MessageListenerAdapter messageListenerAdapter() {
        return new MessageListenerAdapter(new RedisSubscriber());
    }

    @Bean
    @Profile(value = "basic && !cluster !sentinel")
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container =  new RedisMessageListenerContainer();
        container.setConnectionFactory(lettuceBasicConnectionFactory());
        container.addMessageListener(messageListenerAdapter(), new ChannelTopic(topic));
        return container;
    }

}
