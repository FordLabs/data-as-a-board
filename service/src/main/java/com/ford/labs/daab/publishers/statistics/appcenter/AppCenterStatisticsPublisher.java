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

package com.ford.labs.daab.publishers.statistics.appcenter;

import com.ford.labs.daab.config.EventClock;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.statistics.AppCenterApp;
import com.ford.labs.daab.config.event.properties.statistics.AppCenterStatisticsProperties;
import com.ford.labs.daab.config.event.properties.statistics.StatisticsProperties;
import com.ford.labs.daab.event.EventType;
import com.ford.labs.daab.event.StatisticsEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class AppCenterStatisticsPublisher {

    private static final Logger log = getLogger(AppCenterStatisticsPublisher.class);
    private final EventProperties eventProperties;
    private final WebClient webClient;
    private final EventPublishingService eventPublishingService;
    private final EventClock eventClock;


    public AppCenterStatisticsPublisher(EventProperties eventProperties, WebClient webClient, EventPublishingService eventPublishingService, EventClock eventClock, @Value("${appcenter.url:https://api.appcenter.ms}") String url) {
        this.eventProperties = eventProperties;
        this.eventPublishingService = eventPublishingService;
        this.eventClock = eventClock;
        this.webClient = webClient.mutate()
                .baseUrl(url)
                .build();
    }

    @Scheduled(fixedRate = 30000)
    public void pollApps() {
        Flux.fromIterable(getApps())
                .doOnNext(app -> log.info("Processing app center statistics for app " + app.getAppname()))
                .flatMap(app -> retrieveActiveSessionCountsForToday(app, eventProperties.getStatistics().getAppcenter().getToken()))
                .map(appCenterStat -> {
                    var event = new StatisticsEvent();
                    event.setStatistics(List.of(appCenterStat.getStat()));
                    event.setId("statistics.appcenter." + appCenterStat.getApp().getAppname());
                    event.setEventType(EventType.STATISTICS);
                    event.setName(appCenterStat.getApp().getAppname());
                    event.setTime(eventClock.getISOFormattedDateTime());
                    return event;
                })
                .flatMap(eventPublishingService::publish)
                .onErrorMap(e -> new RuntimeException("Error publishing event for app statistics from AppCenter.", e))
                .doOnError(e -> log.error(e.getMessage(), e))
                .blockLast();
    }

    private Flux<AppCenterStatistic> retrieveActiveSessionCountsForToday(AppCenterApp app, String token) {
        String uri = "/v0.1/apps/" + app.getOwnername() + "/" + app.getAppname() + "/analytics/session_counts?start=" + LocalDate.now() + "&interval=P1D";
        return webClient.get().uri(uri).header("X-API-Token", token)
                .retrieve()
                .bodyToFlux(CountResponse.class)
                .map(countResponse -> new AppCenterStatistic(app, new StatisticsEvent.Statistic("active sessions today", countResponse.count)))
                .doOnError(e -> log.error("Error retrieving app with name " + app.getAppname() + " statistics in AppCenter.", e));
    }

    private List<AppCenterApp> getApps() {
        return Optional.of(this.eventProperties)
                .map(EventProperties::getStatistics)
                .map(StatisticsProperties::getAppcenter)
                .map(AppCenterStatisticsProperties::getApps)
                .orElse(Collections.emptyList());
    }
}
