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

package com.ford.labs.daab.subscribers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.daab.event.EventType;
import com.ford.labs.daab.event.Event;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class EventWebSocketHandler implements WebSocketHandler {
    private EventSubscriptionService eventSubscriptionService;
    private ObjectMapper mapper;

    public EventWebSocketHandler(
            EventSubscriptionService eventSubscriptionService,
            ObjectMapper mapper
    ) {
        this.eventSubscriptionService = eventSubscriptionService;
        this.mapper = mapper;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
                Flux.merge(
                        eventSubscriptionService.getCachedEvents(),
                        eventSubscriptionService.subscribe(EventType.ALL)
                )
                        .map(this::eventToString)
                        .filter(Objects::nonNull)
                        .map(session::textMessage)
        );
    }

    private String eventToString(Event event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
