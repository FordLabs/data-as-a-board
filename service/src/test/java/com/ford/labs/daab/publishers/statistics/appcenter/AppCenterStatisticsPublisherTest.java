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

import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.config.EventClock;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.statistics.AppCenterApp;
import com.ford.labs.daab.config.event.properties.statistics.AppCenterStatisticsProperties;
import com.ford.labs.daab.config.event.properties.statistics.HockeyappStatisticsProperties;
import com.ford.labs.daab.config.event.properties.statistics.StatisticsProperties;
import com.ford.labs.daab.event.EventType;
import com.ford.labs.daab.event.StatisticsEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.ford.labs.daab.WireMockExtension.WIREMOCK_URL;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

class AppCenterStatisticsPublisherTest {

    EventClock mockEventClock = mock(EventClock.class);

    EventPublishingService mockEventPublishingService = mock(EventPublishingService.class);

    AppCenterStatisticsPublisher subject;

    EventProperties eventProperties = new EventProperties();
    AppCenterApp app = buildAppCenterApp("testowner", "testapp");
    AppCenterApp app2 = buildAppCenterApp("testowner2", "testapp2");

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    @BeforeEach
    void setup() {
        eventProperties.setStatistics(buildStatisticsProperties());

        subject = new AppCenterStatisticsPublisher(
                eventProperties,
                WebClient.create(),
                mockEventPublishingService,
                mockEventClock,
                WIREMOCK_URL);

        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
        when(mockEventClock.getISOFormattedDateTime()).thenReturn("Hammertime");

        wireMock.getServer().stubFor(get(urlEqualTo(String.format("/v0.1/apps/testowner/testapp/analytics/session_counts?start=%s&interval=P1D", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))))
                .willReturn(okJson("[\n" +
                        "  {\n" +
                        "    \"datetime\": \"2019-05-23T00:00:00Z\",\n" +
                        "    \"count\": 35\n" +
                        "  }\n" +
                        "]")));

        wireMock.getServer().stubFor(get(urlEqualTo(String.format("/v0.1/apps/testowner2/testapp2/analytics/session_counts?start=%s&interval=P1D", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))))
                .willReturn(okJson("[\n" +
                        "  {\n" +
                        "    \"datetime\": \"2019-05-23T00:00:00Z\",\n" +
                        "    \"count\": 3\n" +
                        "  }\n" +
                        "]")));
    }

    @Test
    void eachAppCenterAppIsPublishedAsAStatisticsEvent() {
        subject.pollApps();

        verify(mockEventPublishingService).publish(buildEvent(app, "active sessions today", "35"));
        verify(mockEventPublishingService).publish(buildEvent(app2, "active sessions today", "3"));
    }

    @Test
    public void haltsJobExecutionOnFailureToPublishAnyEvent() {
        when(mockEventPublishingService.publish(buildEvent(app, "active sessions today", "35"))).thenThrow(new RuntimeException("I can't connect to Redis"));

        assertThatThrownBy(() -> subject.pollApps())
                .hasMessage("Error publishing event for app statistics from AppCenter.");
    }

    @Test
    public void doesNothingIfAppCenterPropertyDoesNotExist() {
        EventProperties eventProperties = new EventProperties();
        StatisticsProperties stats = new StatisticsProperties();
        stats.setHockeyapp(new HockeyappStatisticsProperties());
        eventProperties.setStatistics(stats);

        AppCenterStatisticsPublisher publisher = new AppCenterStatisticsPublisher(eventProperties,
                WebClient.create(),
                mockEventPublishingService,
                mockEventClock,
                WIREMOCK_URL);

        publisher.pollApps();

        verify(mockEventPublishingService, never()).publish(any());
    }

    StatisticsProperties buildStatisticsProperties() {
        StatisticsProperties statisticsProperties = new StatisticsProperties();
        AppCenterStatisticsProperties appCenterStatisticsProperties = new AppCenterStatisticsProperties();
        appCenterStatisticsProperties.setToken("token");

        appCenterStatisticsProperties.setApps(Lists.newArrayList(app, app2));
        statisticsProperties.setAppcenter(appCenterStatisticsProperties);
        return statisticsProperties;
    }

    StatisticsEvent buildEvent(AppCenterApp app, String eventName, String eventValue) {
        StatisticsEvent sample = new StatisticsEvent();
        sample.setEventType(EventType.STATISTICS);
        sample.setName(app.getAppname());
        sample.setStatistics(Lists.newArrayList(new StatisticsEvent.Statistic(eventName, eventValue)));
        sample.setId("statistics.appcenter." + app.getAppname());
        sample.setTime(mockEventClock.getISOFormattedDateTime());
        return sample;
    }

    AppCenterApp buildAppCenterApp(String ownername, String appname) {
        AppCenterApp appCenterApp = new AppCenterApp();
        appCenterApp.setOwnername(ownername);
        appCenterApp.setAppname(appname);
        return appCenterApp;
    }
}
