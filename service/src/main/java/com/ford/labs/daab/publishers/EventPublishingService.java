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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.daab.event.Event;
import org.slf4j.Logger;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EventPublishingService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EventPublishingService.class);

    private ReactiveRedisTemplate<String, Event> eventTemplate;

    public EventPublishingService(ReactiveRedisTemplate<String, Event> eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    public Mono<Long> publish(Event event) {
        return eventIsPresentInCache(event.getId())
                .flatMap(eventIsPresent -> {
                    if (eventIsPresent) {
                        return getCachedEvent(event.getId())
                                .map(existingEvent -> existingEvent.equals(event))
                                .flatMap(eventIsUnchanged -> {
                                    if (eventIsUnchanged) {
                                        log.info(String.format("com.ford.labs.daab.event.Event [%s] is unchanged: %s", event.getId(), eventToString(event)));
                                        return Mono.empty();
                                    } else {
                                        return saveAndPublishEvent(event);
                                    }
                                });
                    } else {
                        return saveAndPublishEvent(event);
                    }
                });
    }

    public Mono<Event> getCachedEventOrEmpty(String id) {
        return eventIsPresentInCache(id)
                .flatMap(eventIsPresent -> eventIsPresent
                        ? getCachedEvent(id)
                        : Mono.empty()
                );
    }

    public Mono<Long> delete(String id) {
        return eventIsPresentInCache(id)
                .filter(t -> t)
                .flatMap(isPresent -> getEventHashOperations().remove("event", id));
    }

    private Mono<Boolean> eventIsPresentInCache(String id) {
        return getEventHashOperations()
                .hasKey("event", id);
    }

    private Mono<Event> getCachedEvent(String id) {
        return getEventHashOperations()
                .get("event", id);
    }

    private Mono<Long> saveAndPublishEvent(Event event) {
        log.info(String.format("Publishing com.ford.labs.daab.event.Event: [%s]: %s", event.getId(), eventToString(event)));

        return saveEventToCache(event)
                .then(publishEventToQueue(event));
    }

    private String eventToString(Event event) {
        try {
            return new ObjectMapper().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private Mono<Boolean> saveEventToCache(Event event) {
        return getEventHashOperations()
                .put("event", event.getId(), event);
    }

    private Mono<Long> publishEventToQueue(Event event) {
        return eventTemplate.convertAndSend("event." + event.getId(), event);
    }

    private ReactiveHashOperations<String, String, Event> getEventHashOperations() {
        return eventTemplate.opsForHash(eventTemplate.getSerializationContext());
    }
}
