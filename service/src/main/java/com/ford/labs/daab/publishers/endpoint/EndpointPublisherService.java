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

package com.ford.labs.daab.publishers.endpoint;

import com.ford.labs.daab.event.Event;
import com.ford.labs.daab.publishers.EventPublishingService;
import com.ford.labs.daab.publishers.endpoint.exception.EventAlreadyRegisteredException;
import com.ford.labs.daab.publishers.endpoint.exception.EventNotRegisteredException;
import com.ford.labs.daab.publishers.endpoint.exception.IncorrectKeyException;
import org.slf4j.Logger;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Service
public class EndpointPublisherService {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EndpointPublisherService.class);

    private ReactiveRedisTemplate<String, String> eventRegistrationTemplate;
    private EventPublishingService eventPublishingService;
    static String MAP_KEY = "registeredEvents";

    public EndpointPublisherService(ReactiveRedisTemplate<String, String> eventRegistrationTemplate, EventPublishingService eventPublishingService) {
        this.eventRegistrationTemplate = eventRegistrationTemplate;
        this.eventPublishingService = eventPublishingService;
    }

    public Mono<Void> publishEvent(Event event, String userKey) {
        return isEventRegistered(event.getId())
                .doOnSuccess(isPresent -> {
                    if (!isPresent) {
                        log.warn(String.format("com.ford.labs.daab.event.Event [%s] has been published without registration. ", event.getId()));
                    }
                })
                .flatMap(isPresent -> isPresent
                        ? this.keyMatches(event.getId(), userKey)
                        : Mono.just(true)
                ).flatMap(keyValid -> keyValid
                        ? eventPublishingService.publish(event)
                        : Mono.error(new IncorrectKeyException(event.getId()))
                ).then();
    }

    public Mono<String> registerEvent(String id) {
        return isEventRegistered(id)
                .flatMap(isRegistered -> isRegistered
                        ? Mono.error(new EventAlreadyRegisteredException(id))
                        : createNewKey(id));
    }

    public Mono<Void> deleteEvent(String id, String userKey) {
        return isEventRegistered(id)
                .doOnSuccess(isPresent -> {
                    if (!isPresent) {
                        log.warn(String.format("com.ford.labs.daab.event.Event [%s] has been deleted without registration. ", id));
                    }
                })
                .flatMap(isPresent -> isPresent
                        ? this.keyMatches(id, userKey)
                        : Mono.just(true)
                )
                .flatMap(keyValid -> keyValid
                        ? eventPublishingService.delete(id)
                        : Mono.error(new IncorrectKeyException(id))
                ).then();
    }

    private Mono<String> createNewKey(String id) {
        var newKey = UUID.randomUUID().toString();

        return eventRegistrationTemplate.opsForHash()
                .put(MAP_KEY, id, newKey)
                .then(Mono.just(newKey));
    }

    private Mono<Boolean> keyMatches(String id, String userKey) {
        return getKeyOrError(id)
                .map(key -> Objects.equals(key, userKey));
    }

    private Mono<String> getKeyOrError(String id) {
        return isEventRegistered(id)
                .flatMap(isPresent -> isPresent
                        ? getKey(id)
                        : Mono.error(new EventNotRegisteredException(id))
                );
    }

    private Mono<Boolean> isEventRegistered(String id) {
        return eventRegistrationTemplate.opsForHash().hasKey(MAP_KEY, id);
    }

    private Mono<String> getKey(String id) {
        return eventRegistrationTemplate.opsForHash()
                .get(MAP_KEY, id)
                .cast(String.class);
    }
}
