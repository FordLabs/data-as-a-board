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

package com.ford.labs.daab.publishers.list.retroquest;

import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.list.ListProperties;
import com.ford.labs.daab.config.event.properties.list.RetroquestTeamListProperties;
import com.ford.labs.daab.event.EventType;
import com.ford.labs.daab.event.ListEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;

@Service
public class RetroQuestActionItemsListPublisher {
    private static final Logger log = getLogger(RetroQuestActionItemsListPublisher.class);
    private EventPublishingService service;
    private WebClient client;

    private EventProperties properties;

    public RetroQuestActionItemsListPublisher(
            EventPublishingService service,
            EventProperties properties,
            WebClient client
    ) {
        this.service = service;
        this.client = client;
        this.properties = properties;
    }

    @Scheduled(fixedRate = 60000)
    public void scheduledTask() {
        pollActionItems().blockLast();
    }

    Flux<Long> pollActionItems() {
        return Flux.fromIterable(getTeams())
                .flatMap(team -> getActionItems(team)
                        .map(actionItems -> this.actionItemsToEvent(actionItems, team.getName(), team.getDisplayName()))
                        .filter(Objects::nonNull)
                        .flatMap(service::publish)
                        .doOnError(error -> log.error("Error when polling action items: ", error)));
    }

    private List<RetroquestTeamListProperties> getTeams() {
        return Optional.ofNullable(properties)
                .map(EventProperties::getList)
                .map(ListProperties::getRetroquest)
                .orElseGet(Collections::emptyList);
    }

    private Mono<List<RetroQuestActionItem>> getActionItems(RetroquestTeamListProperties team) {
        var teamClient = client.mutate()
                .baseUrl(team.getUrl())
                .build();

        return teamClient.post()
                .uri("/api/team/login")
                .body(fromObject(team))
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(error -> log.error("Error when logging in: ", error))
                .flatMap(token ->
                        teamClient.get()
                                .uri(String.format("/api/team/%s/action-items", team.getName()))
                                .header("Authorization", "Bearer " + token)
                                .retrieve()
                                .bodyToFlux(RetroQuestActionItem.class)
                                .collectList()
                );
    }

    private ListEvent actionItemsToEvent(List<RetroQuestActionItem> actionItems, String teamId, String displayName) {
        var actionItemsSection = new ListEvent.Section();
        actionItemsSection.setName("Action Items");
        actionItemsSection.setItems(
                actionItems.stream()
                        .filter(item -> !item.isCompleted())
                        .map(item -> StringUtils.hasText(item.getAssignee())
                                ? String.format("%s (%s)", item.getTask(), item.getAssignee())
                                : item.getTask()
                        )
                        .collect(Collectors.toList())
        );

        var event = new ListEvent();
        event.setName(displayName);
        event.setId("list.actionitems." + teamId);
        event.setEventType(EventType.LIST);
        event.setTime(
                actionItems.stream()
                        .filter(item -> !item.isCompleted())
                        .map(RetroQuestActionItem::getDateCreated)
                        .max(LocalDate::compareTo)
                        .map(LocalDate::atStartOfDay)
                        .map(dateTime -> dateTime.atOffset(ZoneOffset.UTC))
                        .map(offsetDateTime -> offsetDateTime.format(DateTimeFormatter.ISO_DATE_TIME))
                        .orElse(null)
        );
        event.setSections(singletonList(actionItemsSection));

        return event;
    }
}
