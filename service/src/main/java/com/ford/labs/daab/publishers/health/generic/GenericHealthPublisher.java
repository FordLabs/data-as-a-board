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

package com.ford.labs.daab.publishers.health.generic;

import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.health.HealthApplication;
import com.ford.labs.daab.config.event.properties.health.HealthProperties;
import com.ford.labs.daab.event.EventLevel;
import com.ford.labs.daab.event.HealthEvent;
import com.ford.labs.daab.event.StatusEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.List.of;

@Service
public class GenericHealthPublisher {
    private EventPublishingService eventPublishingService;
    private final WebClient client;
    private EventProperties eventProperties;

    public GenericHealthPublisher(
            EventPublishingService eventPublishingService,
            WebClient client,
            EventProperties eventProperties
    ) {
        this.eventPublishingService = eventPublishingService;
        this.client = client;
        this.eventProperties = eventProperties;
    }


    @Scheduled(fixedRate = 30000)
    public void pollHealth() {
        for (HealthApplication application : getApplications()) {
            makeRequest(application)
                    .map(isUp -> buildHealthEvent(application, isUp))
                    .flatMap(this::eventOrCachedIfStatusIsSame)
                    .flatMap(eventPublishingService::publish)
                    .block();
        }
    }

    private List<HealthApplication> getApplications() {
        return Optional.of(this.eventProperties)
                .map(EventProperties::getHealth)
                .map(HealthProperties::getApplications)
                .orElse(emptyList());
    }

    private Mono<Boolean> makeRequest(HealthApplication application) {
        return client.get()
                .uri(application.getUrl())
                .headers(headers -> this.buildBasicAuthHeader(application, headers))
                .exchange()
                .map(ClientResponse::statusCode)
                .map(HttpStatus::is2xxSuccessful);
    }

    private void buildBasicAuthHeader(HealthApplication application, HttpHeaders headers) {
        if(application.getUsername() != null && application.getPassword() != null) {
            var rawBasicAuthHeader = String.format("%s:%s", application.getUsername(), application.getPassword());
            headers.put("Authorization", of(String.format("Basic %s", Base64.getEncoder().encodeToString(rawBasicAuthHeader.getBytes()))));
        }
    }

    private StatusEvent buildHealthEvent(HealthApplication application, boolean isUp) {
        var event = new StatusEvent();
        event.setId("health." + application.getId());
        event.setName(application.getName());

        event.setLevel(isUp ? EventLevel.OK : EventLevel.ERROR);
        event.setStatusText(isUp ? "Up" : "Down");

        event.setTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        return event;
    }

    private Mono<StatusEvent> eventOrCachedIfStatusIsSame(StatusEvent newEvent) {
        return eventPublishingService.getCachedEventOrEmpty(newEvent.getId())
                .cast(StatusEvent.class)
                .defaultIfEmpty(newEvent)
                .onErrorResume(error -> Mono.just(newEvent))
                .map(cachedEvent -> Objects.equals(newEvent.getLevel(), cachedEvent.getLevel())
                        ? cachedEvent
                        : newEvent
                );
    }

}
