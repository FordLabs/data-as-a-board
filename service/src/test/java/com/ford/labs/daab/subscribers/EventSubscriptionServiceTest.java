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

package com.ford.labs.daab.subscribers;

import com.ford.labs.daab.model.event.Event;
import com.ford.labs.daab.model.event.EventType;
import com.ford.labs.daab.model.event.HealthEvent;
import com.ford.labs.daab.model.event.JobEvent;
import com.ford.labs.daab.model.event.StatisticsEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventSubscriptionServiceTest {
    private EventSubscriptionService subject;

    private ReactiveRedisTemplate mockEventTemplate;
    private ReactiveHashOperations mockEventHashOperations;

    @BeforeEach
    void setup() {
        mockEventHashOperations = mock(ReactiveHashOperations.class);
        mockEventTemplate = mock(ReactiveRedisTemplate.class);
        this.subject = new EventSubscriptionService(mockEventTemplate);

        when(mockEventTemplate.opsForHash(any())).thenReturn(mockEventHashOperations);
    }

    @Test
    void subscribe_returnsFluxSubscribedToRedisCache() {
        Event jobEvent = new JobEvent();
        Event healthEvent = new HealthEvent();
        Event statisticsEvent = new StatisticsEvent();

        when(mockEventTemplate.listenToPattern(anyString()))
                .thenReturn(Flux.just(
                        new ReactiveSubscription.ChannelMessage<>("event.job.something", jobEvent),
                        new ReactiveSubscription.ChannelMessage<>("event.health.something", healthEvent),
                        new ReactiveSubscription.ChannelMessage<>("event.statistics.something", statisticsEvent)
                ));

        StepVerifier.create(subject.subscribe(EventType.ALL))
                .expectNext(jobEvent)
                .expectNext(healthEvent)
                .expectNext(statisticsEvent)
                .verifyComplete();
    }

    @Test
    void getCachedEvents_returnsFluxContainingCachedEvents() {
        Event jobEvent = new JobEvent();
        Event healthEvent = new HealthEvent();
        Event statisticsEvent = new StatisticsEvent();

        when(mockEventHashOperations.values("event"))
                .thenReturn(Flux.just(
                        jobEvent,
                        healthEvent,
                        statisticsEvent
                ));

        StepVerifier.create(subject.getCachedEvents())
                .expectNext(jobEvent)
                .expectNext(healthEvent)
                .expectNext(statisticsEvent)
                .verifyComplete();

    }
}