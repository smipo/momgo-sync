package com.mongo.sync.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NullValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.Duration;


@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableCaching
public class RedisConfig {


    @Bean
    public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setDefaultSerializer(deserializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair.fromSerializer(deserializer())))
                .build();
    }


    private GenericJackson2JsonRedisSerializer deserializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule((new SimpleModule()).addSerializer(new NullValueSerializer(null)));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    private class NullValueSerializer extends StdSerializer<NullValue> {
        private static final long serialVersionUID = 1999052150548658808L;
        private final String classIdentifier;

        NullValueSerializer(String classIdentifier) {
            super(NullValue.class);
            this.classIdentifier = StringUtils.hasText(classIdentifier) ? classIdentifier : "@class";
        }

        @Override
        public void serialize(NullValue value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeStartObject();
            jgen.writeStringField(this.classIdentifier, NullValue.class.getName());
            jgen.writeEndObject();
        }
    }
}
