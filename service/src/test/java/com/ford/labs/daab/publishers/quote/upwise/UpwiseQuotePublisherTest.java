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

package com.ford.labs.daab.publishers.quote.upwise;

import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.quote.QuoteProperties;
import com.ford.labs.daab.model.event.Event;
import com.ford.labs.daab.model.event.EventLevel;
import com.ford.labs.daab.model.event.QuoteEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;

import static com.ford.labs.daab.WireMockExtension.WIREMOCK_URL;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpwiseQuotePublisherTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    private EventPublishingService mockEventPublishingService = mock(EventPublishingService.class);

    private EventProperties properties = EventProperties.builder()
            .quote(QuoteProperties.builder()
                    .upwise(Collections.emptyMap())
                    .build())
            .build();

    @BeforeEach void setup() {
        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
    }

    @Test void whenEnabled_getsQuoteFromUpwiseAndConvertsToQuoteEvent() {
        wireMock.getServer().stubFor(get(urlEqualTo("/wisdom/random"))
                .willReturn(ok()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"wisdomContent\": \"The only place you should be is in the moment\", \"attribution\": \"Chris Boyer\", \"timeAdded\": \"2019-01-01T00:00:00.000Z\"}"))
        );

        var subject = new UpwiseQuotePublisher(
                mockEventPublishingService,
                properties,
                WebClient.create(),
                WIREMOCK_URL
        );

        subject.pollQuotes()
                .as(StepVerifier::create)
                .expectNext(1L)
                .verifyComplete();

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventPublishingService).publish(eventCaptor.capture());

        assertThat(eventCaptor.getValue())
                .isOfAnyClassIn(QuoteEvent.class)
                .hasFieldOrPropertyWithValue("id", "quote.upwise")
                .hasFieldOrPropertyWithValue("name", "Upwise")
                .hasFieldOrPropertyWithValue("quote", "The only place you should be is in the moment")
                .hasFieldOrPropertyWithValue("author", "Chris Boyer")
                .hasFieldOrPropertyWithValue("time", "2019-01-01T00:00:00.000Z");
    }

    @Test void whenDisabled_doesNotPublish() {
        properties.getQuote().setUpwise(null);

        var disabledSubject = new UpwiseQuotePublisher(
                mockEventPublishingService,
                properties,
                WebClient.create(),
                ""
        );

        disabledSubject.pollQuotes()
                .as(StepVerifier::create)
                .expectNext(0L)
                .verifyComplete();

        verify(mockEventPublishingService, times(0)).publish(any());
    }

    @Test void whenEnabled_whenUpwiseIsDown_publishesUpwiseIsDownQuoteEvent() {
        wireMock.getServer().stubFor(get(urlEqualTo("/wisdom/random"))
                .willReturn(serverError())
        );

        var subject = new UpwiseQuotePublisher(
                mockEventPublishingService,
                properties,
                WebClient.create(),
                WIREMOCK_URL
        );

        subject.pollQuotes()
                .as(StepVerifier::create)
                .expectNext(1L)
                .verifyComplete();

        var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventPublishingService).publish(eventCaptor.capture());

        assertThat(eventCaptor.getValue())
                .isOfAnyClassIn(QuoteEvent.class)
                .hasFieldOrPropertyWithValue("id", "quote.upwise")
                .hasFieldOrPropertyWithValue("name", "Upwise")
                .hasFieldOrPropertyWithValue("level", EventLevel.UNKNOWN);
    }
}
