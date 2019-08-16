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

package com.ford.labs.daab.controllers;

import com.ford.labs.daab.model.event.Event;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@CrossOrigin
public class EventSubmissionController {

    ReactiveRedisTemplate<String, Event> eventTemplate;

    public EventSubmissionController(ReactiveRedisTemplate<String, Event> eventTemplate) {
        this.eventTemplate = eventTemplate;
    }

    @PostMapping("/event/{id}")
    public Mono<Long> submitEvent(@PathVariable String id, Event event) {
        return this.eventTemplate.convertAndSend("event." + id, event);
    }

    @GetMapping(value = "/event/all", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Event> subscribe() {
        return eventTemplate.listenToPattern("event*")
                .map(ReactiveSubscription.Message::getMessage);
    }
}
