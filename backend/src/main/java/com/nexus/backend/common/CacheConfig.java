package com.nexus.backend.common;

import com.nexus.backend.customer.CustomerResponse;
import com.nexus.backend.product.ProductResponse;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Configuration
public class CacheConfig {

    /**
     * Cada cache usa um serializer amarrado ao tipo concreto que ele guarda
     * (List&lt;CustomerResponse&gt;, List&lt;ProductResponse&gt;), em vez do
     * GenericJacksonJsonRedisSerializer polimórfico. Isso evita o problema clássico
     * de type erasure em que, sem metadados de tipo, o Redis desserializa a lista
     * como List&lt;LinkedHashMap&gt; e quebra a resposta com ClassCastException.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(ObjectMapper objectMapper) {
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

        RedisCacheConfiguration customersConfig = baseConfig.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                        typedListSerializer(objectMapper, CustomerResponse.class)));

        RedisCacheConfiguration productsConfig = baseConfig.serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                        typedListSerializer(objectMapper, ProductResponse.class)));

        return builder -> builder
                .withCacheConfiguration("customers", customersConfig)
                .withCacheConfiguration("products", productsConfig);
    }

    private <T> JacksonJsonRedisSerializer<List<T>> typedListSerializer(ObjectMapper objectMapper, Class<T> elementType) {
        JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
        return new JacksonJsonRedisSerializer<>(objectMapper, listType);
    }
}
