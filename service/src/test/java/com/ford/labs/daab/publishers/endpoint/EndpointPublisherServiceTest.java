/*
 * Copyright (c) 2020 Ford Motor Company
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

package com.ford.labs.daab.publishers.endpoint;

import com.ford.labs.daab.event.Event;
import com.ford.labs.daab.publishers.EventPublishingService;
import com.ford.labs.daab.publishers.endpoint.exception.EventAlreadyRegisteredException;
import com.ford.labs.daab.publishers.endpoint.exception.IncorrectKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EndpointPublisherServiceTest {

    private ReactiveRedisTemplate mockRedisTemplate = mock(ReactiveRedisTemplate.class);
    private ReactiveHashOperations mockHashOperations = mock(ReactiveHashOperations.class);
    private EventPublishingService mockService = mock(EventPublishingService.class);

    private EndpointPublisherService subject;

    public EndpointPublisherServiceTest() {
        this.subject = new EndpointPublisherService(mockRedisTemplate, mockService);
    }

    @BeforeEach
    void setup() {
        when(mockRedisTemplate.opsForHash()).thenReturn(mockHashOperations);

        when(mockService.publish(any())).thenReturn(Mono.just(1L));
        when(mockService.delete(any())).thenReturn(Mono.just(1L));
    }

    @Test
    public void publishEvent_publishesIfEventIsNotRegistered() {
        String id = "test.id";
        Event event = new Event();
        event.setId(id);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(false));

        subject.publishEvent(event, "someKey")
                .doOnSuccess(v -> {
                    verify(mockService).publish(eventCaptor.capture());

                    assertThat(eventCaptor.getValue()).isEqualTo(event);
                })
                .block();
    }

    @Test
    public void publishEvent_returnsErrorIfKeyIncorrect() {
        String id = "test.id";
        Event event = new Event();
        event.setId(id);

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(true));
        when(mockHashOperations.get(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just("correctKey"));

        subject.publishEvent(event, "incorrectKey")
                .as(StepVerifier::create)
                .expectError(IncorrectKeyException.class)
                .verify();
    }

    @Test
    public void publishEvent_publishesIfEventIsRegisteredAndKeyIsCorrect() {
        String id = "test.id";
        Event event = new Event();
        event.setId(id);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(true));
        when(mockHashOperations.get(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just("key"));

        subject.publishEvent(event, "key")
                .doOnSuccess(v -> {
                    verify(mockService).publish(eventCaptor.capture());

                    assertThat(eventCaptor.getValue()).isEqualTo(event);
                })
                .block();
    }

    @Test
    public void deleteEvent_deletesIfEventIsNotRegistered() {
        String id = "test.id";
        Event event = new Event();
        event.setId(id);

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(false));

        subject.deleteEvent("test.id", "key")
                .doOnSuccess(v -> verify(mockService).delete(id))
                .block();
    }

    @Test
    public void deleteEvent_returnsErrorIfKeyIncorrect() {
        String id = "test.id";
        Event event = new Event();
        event.setId(id);

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(true));
        when(mockHashOperations.get(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just("correctKey"));

        subject.deleteEvent("test.id", "key")
                .as(StepVerifier::create)
                .expectError(IncorrectKeyException.class)
                .verify();
    }

    @Test
    public void deleteEvent_deletesIfEventIsRegisteredAndKeyIsCorrect() {
        String id = "test.id";
        Event event = new Event();
        event.setId(id);

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(true));
        when(mockHashOperations.get(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just("key"));


        subject.deleteEvent("test.id", "key")
                .doOnSuccess(v -> verify(mockService).delete(id))
                .block();
    }

    @Test
    public void registerEvent_returnsErrorIfAlreadyRegistered() {
        String id = "test.id";

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(true));

        subject.registerEvent(id)
                .as(StepVerifier::create)
                .expectError(EventAlreadyRegisteredException.class)
                .verify();
    }

    @Test
    public void registerEvent_savesNewKeyToHashAndReturnsIt() {
        String id = "test.id";

        when(mockHashOperations.hasKey(eq(EndpointPublisherService.MAP_KEY), eq(id))).thenReturn(Mono.just(false));
        when(mockHashOperations.put(any(), any(), any())).thenReturn(Mono.empty());

        subject.registerEvent(id)
                .as(StepVerifier::create)
                .assertNext(key -> {
                    assertThat(key).isEqualTo(UUID.fromString(key).toString());
                    verify(mockHashOperations).put(EndpointPublisherService.MAP_KEY, id, key);
                })
                .expectComplete()
                .verify();
    }
}