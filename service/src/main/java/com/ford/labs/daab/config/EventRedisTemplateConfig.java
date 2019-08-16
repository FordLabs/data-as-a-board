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

import com.ford.labs.daab.model.event.Event;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
public class EventRedisTemplateConfig {
    @Bean
    public Jackson2JsonRedisSerializer<Event> eventSerializer() {
        return new Jackson2JsonRedisSerializer<>(Event.class);
    }

    @Bean
    public RedisSerializationContext<String, Event> eventSerializationContext(Jackson2JsonRedisSerializer<Event> eventSerializer) {
        RedisSerializationContext.RedisSerializationContextBuilder<String, Event> builder = RedisSerializationContext.newSerializationContext(eventSerializer);
        return builder.value(eventSerializer).build();
    }

    @Bean
    public ReactiveRedisTemplate<String, Event> eventRedisTemplate(ReactiveRedisConnectionFactory factory, RedisSerializationContext<String, Event> eventSerializationContext) {
        return new ReactiveRedisTemplate<>(factory, eventSerializationContext);
    }

    @Bean
    public ReactiveHashOperations<String, String, Event> eventRedisHashOperations(@Qualifier("eventRedisTemplate") ReactiveRedisTemplate<String, Event> reactiveRedisTemplate, RedisSerializationContext<String, Event> eventSerializationContext) {
        return reactiveRedisTemplate.opsForHash(eventSerializationContext);
    }
}
