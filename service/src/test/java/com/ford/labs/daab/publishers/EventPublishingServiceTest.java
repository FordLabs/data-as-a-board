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

package com.ford.labs.daab.publishers;

import com.ford.labs.daab.event.HealthEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventPublishingServiceTest {
    ReactiveRedisTemplate mockEventTemplate = mock(ReactiveRedisTemplate.class);
    ReactiveHashOperations mockEventHashOperations = mock(ReactiveHashOperations.class);

    EventPublishingService subject;

    @BeforeEach
    void setup() {
        subject = new EventPublishingService(mockEventTemplate);

        when(mockEventTemplate.opsForHash(any())).thenReturn(mockEventHashOperations);
    }

    @Test
    void publish_ifNotPresent_savesInCacheAndPublishes() {
        HealthEvent event = new HealthEvent();
        event.setId("id");
        event.setName("Health com.ford.labs.daab.event.Event Name");
        event.setStatus(HealthEvent.Status.UP);
        event.setTime("2018-01-01T00:00:00.000Z");

        when(mockEventHashOperations.hasKey(anyString(), anyString())).thenReturn(Mono.just(false));
        when(mockEventHashOperations.put(anyString(), anyString(), any())).thenReturn(Mono.just(true));
        when(mockEventTemplate.convertAndSend(anyString(), any())).thenReturn(Mono.just(1L));


        StepVerifier.create(subject.publish(event))
                .expectNext(1L)
                .verifyComplete();

        verify(mockEventHashOperations).hasKey("event", "id");

        verify(mockEventHashOperations).put("event", "id", event);
        verify(mockEventTemplate).convertAndSend("event.id", event);
    }

    @Test
    void publish_ifPresent_ifUnchanged_doesNotSaveOrPublish() {
        HealthEvent event = new HealthEvent();
        event.setId("id");
        event.setName("Health com.ford.labs.daab.event.Event Name");
        event.setStatus(HealthEvent.Status.UP);
        event.setTime("2018-01-01T00:00:00.000Z");

        when(mockEventHashOperations.hasKey(anyString(), anyString())).thenReturn(Mono.just(true));
        when(mockEventHashOperations.get(anyString(), anyString())).thenReturn(Mono.just(event));
        when(mockEventHashOperations.put(anyString(), anyString(), any())).thenReturn(Mono.just(true));
        when(mockEventTemplate.convertAndSend(anyString(), any())).thenReturn(Mono.just(1L));

        StepVerifier.create(subject.publish(event))
                .verifyComplete();

        verify(mockEventHashOperations).hasKey("event", "id");
        verify(mockEventHashOperations).get("event", "id");

        verify(mockEventHashOperations, times(0)).put("event", "id", event);
        verify(mockEventTemplate, times(0)).convertAndSend("event.id", event);
    }

    @Test
    void publish_ifPresent_ifChanged_savesInCacheAndPublishes() {
        HealthEvent cachedEvent = new HealthEvent();
        cachedEvent.setId("id");
        cachedEvent.setName("Health com.ford.labs.daab.event.Event Name");
        cachedEvent.setStatus(HealthEvent.Status.UP);
        cachedEvent.setTime("2018-01-01T00:00:00.000Z");

        HealthEvent newEvent = new HealthEvent();
        newEvent.setId("id");
        newEvent.setName("Health com.ford.labs.daab.event.Event Name");
        newEvent.setStatus(HealthEvent.Status.DOWN);
        newEvent.setTime("2018-01-02T00:00:00.000Z");

        when(mockEventHashOperations.hasKey(anyString(), anyString())).thenReturn(Mono.just(true));
        when(mockEventHashOperations.get(anyString(), anyString())).thenReturn(Mono.just(cachedEvent));
        when(mockEventHashOperations.put(anyString(), anyString(), any())).thenReturn(Mono.just(true));
        when(mockEventTemplate.convertAndSend(anyString(), any())).thenReturn(Mono.just(1L));

        StepVerifier.create(subject.publish(newEvent))
                .expectNext(1L)
                .verifyComplete();

        verify(mockEventHashOperations).hasKey("event", "id");
        verify(mockEventHashOperations).get("event", "id");

        verify(mockEventHashOperations).put("event", "id", newEvent);
        verify(mockEventTemplate).convertAndSend("event.id", newEvent);
    }

    @Test
    void delete_ifNotPresent_doesNothing() {
        when(mockEventHashOperations.hasKey(anyString(), anyString())).thenReturn(Mono.just(false));
        when(mockEventHashOperations.put(anyString(), anyString(), any())).thenReturn(Mono.just(true));
        when(mockEventTemplate.convertAndSend(anyString(), any())).thenReturn(Mono.just(1L));


        StepVerifier.create(subject.delete("somevent.toDelete"))
                .verifyComplete();

        verify(mockEventHashOperations, times(0)).remove("event", "somevent.toDelete");
    }

    @Test
    void delete_ifPresent_deletesEvent() {
        when(mockEventHashOperations.hasKey(anyString(), anyString())).thenReturn(Mono.just(true));
        when(mockEventHashOperations.put(anyString(), anyString(), any())).thenReturn(Mono.just(true));
        when(mockEventHashOperations.remove(anyString(), anyString())).thenReturn(Mono.just(1L));
        when(mockEventTemplate.convertAndSend(anyString(), any())).thenReturn(Mono.just(1L));


        StepVerifier.create(subject.delete("somevent.toDelete"))
                .expectNext(1L)
                .verifyComplete();

        verify(mockEventHashOperations).remove("event", "somevent.toDelete");
    }
}