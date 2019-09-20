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

import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.quote.QuoteProperties;
import com.ford.labs.daab.event.EventLevel;
import com.ford.labs.daab.event.QuoteEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@Service
public class UpwiseQuotePublisher {
    private static final QuoteEvent DOWN = new QuoteEvent();

    static {
        DOWN.setId("quote.upwise");
        DOWN.setName("Upwise");
        DOWN.setLevel(EventLevel.UNKNOWN);
        DOWN.setQuote("Upwise is down!");
        DOWN.setAuthor("Probably John Martin");
        DOWN.setTime(OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
    }

    private final EventPublishingService service;
    private final boolean isEnabled;
    private final WebClient client;

    public UpwiseQuotePublisher(
            EventPublishingService service,
            EventProperties eventProperties,
            WebClient client,
            @Value("${upwise.url:https://upwise.cfapps.io}") String url
    ) {
        this.service = service;
        this.isEnabled = Optional.ofNullable(eventProperties)
                .map(EventProperties::getQuote)
                .map(QuoteProperties::getUpwise)
                .isPresent();
        this.client = client.mutate()
                .baseUrl(url)
                .build();
    }

    @Scheduled(fixedRate = 30000)
    public void scheduledTask() {
        pollQuotes().block();
    }

    Mono<Long> pollQuotes() {
        return isEnabled
                ? getRandomWisdom()
                .filter(Objects::nonNull)
                .map(this::wisdomToEvent)
                .onErrorResume(throwable -> Mono.just(DOWN))
                .flatMap(service::publish)
                : Mono.just(0L);
    }

    private Mono<WisdomResponse> getRandomWisdom() {
        return client.get()
                .uri("/wisdom/random")
                .retrieve()
                .bodyToMono(WisdomResponse.class);
    }

    private QuoteEvent wisdomToEvent(WisdomResponse wisdom) {
        var event = new QuoteEvent();
        event.setId("quote.upwise");
        event.setName("Upwise");
        event.setQuote(wisdom.getWisdomContent());
        event.setAuthor(wisdom.getAttribution());
        event.setTime(wisdom.getTimeAdded());
        return event;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class WisdomResponse {
        private String wisdomContent;
        private String attribution;
        private String timeAdded;
    }
}
