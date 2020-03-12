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

package com.ford.labs.daab.publishers.health.generic;

import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.health.HealthApplication;
import com.ford.labs.daab.config.event.properties.health.HealthProperties;
import com.ford.labs.daab.event.EventLevel;
import com.ford.labs.daab.event.HealthEvent;
import com.ford.labs.daab.event.StatusEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenericHealthPublisherTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    EventPublishingService mockEventPublishingService = mock(EventPublishingService.class);
    EventProperties eventProperties = new EventProperties();
    HealthProperties healthProperties = new HealthProperties();

    GenericHealthPublisher subject = new GenericHealthPublisher(
            mockEventPublishingService,
            WebClient.create(),
            eventProperties
    );

    @BeforeEach
    void setup() {
        healthProperties = new HealthProperties();
        eventProperties.setHealth(healthProperties);

        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
        when(mockEventPublishingService.getCachedEventOrEmpty(anyString())).thenReturn(Mono.empty());
    }

    @Test
    void pollHealth_getsHealthStatusAndPublishesEventIfStatusIsChanged() {
        List<HealthApplication> applications = asList(
                new HealthApplication("up", "IsUp", "http://localhost:8123/up", null, null),
                new HealthApplication("down", "IsDown", "http://localhost:8123/down", "username", "password"),
                new HealthApplication("downbutcached", "IsDownButCached", "http://localhost:8123/downButCached", null, null)
        );

        StatusEvent cachedEvent = new StatusEvent();
        cachedEvent.setId("health.id3");
        cachedEvent.setLevel(EventLevel.ERROR);
        cachedEvent.setName("IsDownButCached");
        cachedEvent.setStatusText("Down");
        cachedEvent.setTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        when(mockEventPublishingService.getCachedEventOrEmpty("health.downbutcached")).thenReturn(Mono.just(cachedEvent));

        healthProperties.setApplications(applications);

        wireMock.getServer().stubFor(get(urlEqualTo("/up")).willReturn(ok()));
        wireMock.getServer().stubFor(get(urlEqualTo("/down")).willReturn(serverError()));
        wireMock.getServer().stubFor(get(urlEqualTo("/downButCached")).willReturn(serverError()));

        subject.pollHealth();

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("health.up")
                                && event.getName().equals("IsUp")
                                && ((StatusEvent) event).getLevel().equals(EventLevel.OK)
                                && ((StatusEvent) event).getStatusText().equals("Up")
                ));

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("health.down")
                                && event.getName().equals("IsDown")
                                && ((StatusEvent) event).getLevel().equals(EventLevel.ERROR)
                                && ((StatusEvent) event).getStatusText().equals("Down")
                ));

        verify(mockEventPublishingService, times(0))
                .publish(argThat(event -> event.getId().equals("health.downbutcached")));
    }
}
