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

import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.event.HealthEvent;
import com.ford.labs.daab.event.JobEvent;
import com.ford.labs.daab.subscribers.EventSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SlackSubscriberTest {
    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    SlackSubscriber subject;

    SlackClient client = new SlackClient(WebClient.create(), "http://localhost:8123");
    EventSubscriptionService mockEventSubscriptionService = mock(EventSubscriptionService.class);
    SlackClientProperties configuration = new SlackClientProperties("mockToken", "channel");

    @BeforeEach
    void setup() {
        this.subject = new SlackSubscriber(
                mockEventSubscriptionService,
                client,
                configuration
        );
    }

    @Test
    void subscribeToEvents_postsEachFailedJobEventToSlack() {
        wireMock.getServer().stubFor(post(urlEqualTo("/chat.postMessage")).willReturn(okJson("{}")));

        OffsetDateTime now = OffsetDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_DATE_TIME);

        JobEvent successfulJobEvent = new JobEvent();
        successfulJobEvent.setId("job.success");
        successfulJobEvent.setName("job.success");
        successfulJobEvent.setTime(timestamp);
        successfulJobEvent.setStatus(JobEvent.Status.SUCCESS);

        JobEvent failedJobEvent = new JobEvent();
        failedJobEvent.setId("job.failure");
        failedJobEvent.setName("FAILURE");
        failedJobEvent.setUrl("fakeurl");
        failedJobEvent.setTime(timestamp);
        failedJobEvent.setStatus(JobEvent.Status.FAILURE);

        when(mockEventSubscriptionService.subscribe(anyString()))
                .thenReturn(Flux.just(
                        successfulJobEvent,
                        failedJobEvent
                ));

        StepVerifier.create(subject.subscribeToEvents())
                .expectNextCount(1)
                .verifyComplete();

        wireMock.getServer().verify(
                postRequestedFor(urlEqualTo("/chat.postMessage"))
                        .withHeader("Authorization", equalTo("Bearer mockToken"))
                        .withRequestBody(equalToJson("{\"channel\": \"channel\", \"text\": \"Job FAILURE has failed. \", \"as_user\": true, \"attachments\": [{\"fallback\": \"Job FAILURE has failed. \", \"color\": \"#B71C1C\", \"title\": \"Job FAILURE has failed. \", \"title_link\": \"fakeurl\", \"ts\": " + now.toEpochSecond() + "}] }"))
        );
    }

    @Test
    void subscribeToEvents_ifEventFailsAndThenSucceeds_postSuccessToSlack() {
        wireMock.getServer().stubFor(post(urlEqualTo("/chat.postMessage")).willReturn(okJson("{}")));

        OffsetDateTime now = OffsetDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ISO_DATE_TIME);

        HealthEvent firstHealthEvent = new HealthEvent();
        firstHealthEvent.setId("job.willChange");
        firstHealthEvent.setName("Will Change");
        firstHealthEvent.setTime(timestamp);
        firstHealthEvent.setStatus(HealthEvent.Status.DOWN);

        when(mockEventSubscriptionService.subscribe(anyString()))
                .thenReturn(Flux.just(firstHealthEvent));

        StepVerifier.create(subject.subscribeToEvents())
                .expectNextCount(1)
                .verifyComplete();

        String failureMessage = "App Will Change is down! ";
        wireMock.getServer().verify(
                postRequestedFor(urlEqualTo("/chat.postMessage"))
                        .withHeader("Authorization", equalTo("Bearer mockToken"))
                        .withRequestBody(equalToJson("{\"channel\": \"channel\", \"text\": \"" + failureMessage + "\", \"as_user\": true, \"attachments\": [{\"fallback\": \"" + failureMessage + "\", \"color\": \"#B71C1C\", \"title\": \"" + failureMessage + "\", \"title_link\": null, \"ts\": " + now.toEpochSecond() + "}] }"))
        );

        HealthEvent secondHealthEvent = new HealthEvent();
        secondHealthEvent.setId("job.willChange");
        secondHealthEvent.setName("Will Change");
        secondHealthEvent.setTime(timestamp);
        secondHealthEvent.setStatus(HealthEvent.Status.UP);


        when(mockEventSubscriptionService.subscribe(anyString()))
                .thenReturn(Flux.just(secondHealthEvent));

        StepVerifier.create(subject.subscribeToEvents())
                .expectNextCount(1)
                .verifyComplete();

        String successMessage = "App Will Change is back up! ";
        wireMock.getServer().verify(
                postRequestedFor(urlEqualTo("/chat.postMessage"))
                        .withHeader("Authorization", equalTo("Bearer mockToken"))
                        .withRequestBody(equalToJson("{\"channel\": \"channel\", \"text\": \"" + successMessage + "\", \"as_user\": true, \"attachments\": [{\"fallback\": \"" + successMessage + "\", \"color\": \"#1B5E20\", \"title\": \"" + successMessage + "\", \"title_link\": null, \"ts\": " + now.toEpochSecond() + "}] }"))
        );

        StepVerifier.create(subject.subscribeToEvents())
                .verifyComplete();
    }
}