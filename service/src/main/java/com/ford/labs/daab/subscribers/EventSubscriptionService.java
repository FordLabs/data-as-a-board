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

import com.ford.labs.daab.model.event.EventType;
import com.ford.labs.daab.model.event.Event;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EventSubscriptionService {

    private ReactiveRedisTemplate<String, Event> eventTemplate;

    public EventSubscriptionService(ReactiveRedisTemplate<String, Event> eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    public Flux<Event> subscribe(String eventType) {
        return eventTemplate.listenToPattern(eventTypeToPattern(eventType))
                .map(ReactiveSubscription.Message::getMessage);
    }

    public Flux<Event> getCachedEvents() {
        ReactiveHashOperations<String, String, Event> eventHashOperations = eventTemplate.opsForHash(eventTemplate.getSerializationContext());

        return getEventHashOperations().values("event");
    }

    public Mono<Event> getCachedEventOrEmpty(String id) {
        return eventIsPresentInCache(id)
                .flatMap(eventIsPresent -> eventIsPresent
                        ? getCachedEvent(id)
                        : Mono.empty()
                );
    }
    private Mono<Boolean> eventIsPresentInCache(String id) {
        return getEventHashOperations()
                .hasKey("event", id);
    }

    private Mono<Event> getCachedEvent(String id) {
        return getEventHashOperations()
                .get("event", id);
    }

    private ReactiveHashOperations<String, String, Event> getEventHashOperations() {
        return eventTemplate.opsForHash(eventTemplate.getSerializationContext());
    }

    private static String eventTypeToPattern(String eventType) {
        switch (eventType) {
            case EventType.HEALTH:
                return "event.health*";
            case EventType.JOB:
                return "event.job*";
            case EventType.FIGURE:
                return "event.figure*";
            case EventType.QUOTE:
                return "event.quote*";
            case EventType.PERCENTAGE:
                return "event.percentage*";
            case EventType.STATISTICS:
                return "event.statistics*";
            case EventType.WEATHER:
                return "event.weather*";
            case EventType.LIST:
                return "event.list*";
            case EventType.IMAGE:
                return "event.image*";
            case EventType.UNKNOWN:
                return "event.unknown*";
            case EventType.ALL:
            default:
                return "event*";
        }
    }
}
