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

package com.ford.labs.daab.publishers.list.retroquest;

import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.list.ListProperties;
import com.ford.labs.daab.config.event.properties.list.RetroquestTeamListProperties;
import com.ford.labs.daab.model.event.Event;
import com.ford.labs.daab.model.event.ListEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static com.ford.labs.daab.WireMockExtension.WIREMOCK_URL;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class RetroQuestActionItemsListPublisherTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    private EventPublishingService mockEventPublishingService = mock(EventPublishingService.class);
    private EventProperties properties = EventProperties.builder()
            .list(ListProperties.builder().build())
            .build();

    @BeforeEach void setup() {
        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
    }

    @Test void whenConfigurationPresent_getsActionItemsFromRetroquestAndConvertsToListEvent() {
        wireMock.getServer().stubFor(post(urlEqualTo("/api/team/login"))
                .willReturn(ok().withBody("eyLmao"))
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/api/team/team/action-items"))
                .willReturn(ok().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody("[{\"id\": 1, \"task\": \"Do something\", \"teamid\": \"team\", \"completed\": false, \"assignee\": \"The User\", \"dateCreated\": \"2019-01-01\"}, {\"id\": 2, \"task\": \"Don't do something\", \"teamid\": \"team\", \"completed\": true, \"assignee\": \"The User\", \"dateCreated\": \"2019-01-01\"}]"))
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/api/team/otherTeam/action-items"))
                .willReturn(ok().withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE).withBody("[{\"id\": 3, \"task\": \"Do something\", \"teamid\": \"team\", \"completed\": false, \"assignee\": \"The User\", \"dateCreated\": \"2019-01-01\"}, {\"id\": 4, \"task\": \"Don't do something\", \"teamid\": \"team\", \"completed\": false, \"assignee\": \"\", \"dateCreated\": \"2019-01-04\"}]"))
        );

        var subject = new RetroQuestActionItemsListPublisher(
                mockEventPublishingService,
                properties,
                WebClient.create()
        );

        properties.getList().setRetroquest(
                List.of(
                        new RetroquestTeamListProperties("team", "The Team", "password", WIREMOCK_URL),
                        new RetroquestTeamListProperties("otherTeam", "The Other Team", "other password", WIREMOCK_URL)
                )
        );

        subject.pollActionItems()
                .as(StepVerifier::create)
                .expectNext(1L)
                .expectNext(1L)
                .verifyComplete();

        wireMock.getServer().verify(
                getRequestedFor(urlEqualTo("/api/team/team/action-items"))
                        .withHeader("Authorization", equalTo("Bearer eyLmao"))
        );

        wireMock.getServer().verify(
                getRequestedFor(urlEqualTo("/api/team/otherTeam/action-items"))
                        .withHeader("Authorization", equalTo("Bearer eyLmao"))
        );

        var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventPublishingService, times(2)).publish(eventCaptor.capture());

        var publishedEvents = eventCaptor.getAllValues();
        assertThat(publishedEvents)
                .hasSize(2)
                .anyMatch(event -> event.getId().equals("list.actionitems.team"))
                .anyMatch(event -> event.getId().equals("list.actionitems.otherTeam"));


        ListEvent team = (ListEvent) publishedEvents.stream().filter(event -> event.getId().equals("list.actionitems.team")).findFirst().get();
        assertThat(team)
                .isOfAnyClassIn(ListEvent.class)
                .hasFieldOrPropertyWithValue("id", "list.actionitems.team")
                .hasFieldOrPropertyWithValue("name", "The Team")
                .hasFieldOrPropertyWithValue("time", "2019-01-01T00:00:00Z");

        assertThat(team.getSections())
                .hasSize(1)
                .contains(new ListEvent.Section("Action Items", List.of("Do something (The User)")));

        ListEvent otherTeam = (ListEvent) publishedEvents.stream().filter(event -> event.getId().equals("list.actionitems.otherTeam")).findFirst().get();
        assertThat(otherTeam)
                .isOfAnyClassIn(ListEvent.class)
                .hasFieldOrPropertyWithValue("id", "list.actionitems.otherTeam")
                .hasFieldOrPropertyWithValue("name", "The Other Team")
                .hasFieldOrPropertyWithValue("time", "2019-01-04T00:00:00Z");

        assertThat(otherTeam.getSections())
                .hasSize(1)
                .contains(new ListEvent.Section("Action Items", List.of("Do something (The User)", "Don't do something")));
    }
}