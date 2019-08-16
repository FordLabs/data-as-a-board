/*
 * Copyright (c) 2019 Ford Motor Company
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 */

package com.ford.labs.daab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class EventRegistrationRedisTemplateConfig {
    @Bean
    public Jackson2JsonRedisSerializer<String> eventRegistrationSerializer() {
        return new Jackson2JsonRedisSerializer<>(String.class);
    }

    @Bean
    public RedisSerializationContext<String, String> eventRegistrationSerializationContext(Jackson2JsonRedisSerializer<String> eventSerializer) {
        RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder = RedisSerializationContext.newSerializationContext(eventSerializer);
        return builder.value(eventSerializer).build();
    }

    @Bean
    public ReactiveRedisTemplate<String, String> eventRegistrationRedisTemplate(ReactiveRedisConnectionFactory factory, RedisSerializationContext<String, String> eventSerializationContext) {
        return new ReactiveRedisTemplate<>(factory, eventSerializationContext);
    }

    @Bean
    public ReactiveHashOperations<String, String, String> eventRegistrationRedisHashOperations(ReactiveRedisTemplate<String, String> reactiveRedisTemplate, RedisSerializationContext<String, String> eventSerializationContext) {
        return reactiveRedisTemplate.opsForHash(eventSerializationContext);
    }
}
