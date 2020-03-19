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

package com.ford.labs.daab.subscribers.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.daab.event.*;
import com.ford.labs.daab.subscribers.EventSubscriptionService;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
public class SlackSubscriber {
    private CopyOnWriteArraySet<String> badEvents = new CopyOnWriteArraySet<>();

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(SlackSubscriber.class);

    private static final Map<EventLevel, String> LEVEL_COLORS = Map.of(
            EventLevel.OK, "#1B5E20",
            EventLevel.WARN, "#F57F17",
            EventLevel.ERROR, "#B71C1C"
    );

    private EventSubscriptionService eventSubscriptionService;
    private SlackClient client;
    private SlackClientProperties properties;

    public SlackSubscriber(
            EventSubscriptionService eventSubscriptionService,
            SlackClient client,
            SlackClientProperties properties) {
        this.eventSubscriptionService = eventSubscriptionService;
        this.client = client;
        this.properties = properties;
    }

    @Async
    public CompletableFuture<List<PostMessageResponse>> subscriptionToFuture() {
        return subscribeToEvents()
                .collectList()
                .toFuture();
    }

    Flux<PostMessageResponse> subscribeToEvents() {
        return eventSubscriptionService
                .subscribe(EventType.ALL)
                .filter(Objects::nonNull)
                .doOnEach(event -> {
                    if (event.get() != null) {
                        log.info("com.ford.labs.daab.event.Event: " + event.get().getId());
                        log.info("Level: " + event.get().getLevel());
                        log.info("Current bad events: " + badEvents.toString());
                    }
                })
                .filter(event -> {
                    boolean isBadLevel = event.getLevel().equals(EventLevel.WARN) || event.getLevel().equals(EventLevel.ERROR);
                    boolean isGoodButRestored = event.getLevel().equals(EventLevel.OK) && badEvents.contains(event.getId());

                    return isBadLevel || isGoodButRestored;
                })
                .doOnEach(event -> {
                    if (event.get() != null) {
                        var level = event.get().getLevel();
                        var id = event.get().getId();
                        if (level.equals(EventLevel.WARN) || level.equals(EventLevel.ERROR)) {
                            badEvents.add(id);
                        } else if (level.equals(EventLevel.OK)) {
                            badEvents.remove(id);
                        }
                    }
                })
                .map(this::requestFromEvent)
                .filter(Objects::nonNull)
                .flatMap(request -> client.postMessage(request, properties.getToken()));
    }

    PostMessageRequest requestFromEvent(Event event) {
        switch (event.getEventType()) {
            case EventType.JOB:
                return requestFromJobEvent((JobEvent) event);
            case EventType.HEALTH:
                return requestFromHealthEvent((HealthEvent) event);
            default:
                return requestFromUnknownEvent(event);
        }
    }

    private PostMessageRequest requestFromJobEvent(JobEvent event) {
        String message = "Job " + event.getName() + " has failed. ";
        return new PostMessageRequest(
                properties.getChannel(),
                message,
                true,
                Collections.singletonList(
                        new PostMessageRequest.Attachment(
                                message,
                                "#B71C1C",
                                message,
                                event.getUrl(),
                                OffsetDateTime.parse(event.getTime()).toEpochSecond()
                        )
                )
        );
    }

    private PostMessageRequest requestFromHealthEvent(HealthEvent event) {
        String message = event.getLevel().equals(EventLevel.OK)
                ? String.format("App %s is back up! ", event.getName())
                : String.format("App %s is down! ", event.getName());

        return new PostMessageRequest(
                properties.getChannel(),
                message,
                true,
                Collections.singletonList(
                        new PostMessageRequest.Attachment(
                                message,
                                LEVEL_COLORS.get(event.getLevel()),
                                message,
                                null,
                                OffsetDateTime.parse(event.getTime()).toEpochSecond()
                        )
                )
        );
    }

    private PostMessageRequest requestFromUnknownEvent(Event event) {
        ObjectMapper mapper = new ObjectMapper();

        String messageHeader = "";
        if (event.getLevel().equals(EventLevel.WARN)) {
            messageHeader = "Check out " + event.getName() + ". ";
        }
        if (event.getLevel().equals(EventLevel.ERROR)) {
            messageHeader = "Something is wrong with " + event.getName() + "! ";
        }

        String messageBody;
        try {
            messageBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
        } catch (JsonProcessingException e) {
            messageBody = "";
        }

        String fullMessage = String.format("%s\n```\n%s\n```", messageHeader, messageBody);

        return new PostMessageRequest(
                properties.getChannel(),
                fullMessage,
                true,
                Collections.emptyList()
        );
    }


}
