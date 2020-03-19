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

package com.ford.labs.daab.publishers.weather.nws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.weather.NwsWeatherProperties;
import com.ford.labs.daab.config.event.properties.weather.WeatherProperties;
import com.ford.labs.daab.event.Event;
import com.ford.labs.daab.event.WeatherEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.ford.labs.daab.WireMockExtension.WIREMOCK_URL;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class NwsWeatherPublisherTest {

    EventPublishingService mockEventPublishingService = mock(EventPublishingService.class);

    NwsWeatherPublisher subject;

    EventProperties eventProperties = new EventProperties();
    WeatherProperties weatherProperties = new WeatherProperties();

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    static ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        weatherProperties.setNws(List.of(
                new NwsWeatherProperties("valid", "Valid", "22.222", "44.444")
        ));
        eventProperties.setWeather(weatherProperties);

        subject = new NwsWeatherPublisher(
                mockEventPublishingService,
                WebClient.create(),
                eventProperties,
                WIREMOCK_URL
        );

        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
    }

    @Test
    void whenConfigurationPresent_getsWeatherFromNwsAndConvertsToWeatherEvent() throws JsonProcessingException {
        var validPointsResponse = new NwsPointsResponse(
                new NwsPointsResponse.Properties("/forecast/valid/something")
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/points/22.222,44.444"))
                .willReturn(ok()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsBytes(validPointsResponse)))
        );

        var validForecastResponse = new NwsForecastResponse(
                new NwsForecastResponse.Properties("2019-01-01T00:00:00.000Z", List.of(
                        new NwsForecastResponse.Properties.Period(0, "F", "Mostly Cold")
                ))
        );

        wireMock.getServer().stubFor(get(urlEqualTo(validPointsResponse.properties.forecast))
                .willReturn(ok()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(mapper.writeValueAsBytes(validForecastResponse)))
        );

        subject.pollWeather();


        var eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockEventPublishingService, times(1)).publish(eventCaptor.capture());

        var publishedEvents = eventCaptor.getAllValues();
        assertThat(publishedEvents)
                .hasSize(1)
                .anyMatch(event -> event.getId().equals("weather.valid"));

        var validWeather = publishedEvents.stream().filter(event -> event.getId().equals("weather.valid")).findFirst().orElse(null);
        assertThat(validWeather)
                .isNotNull()
                .isOfAnyClassIn(WeatherEvent.class)
                .hasFieldOrPropertyWithValue("id", "weather.valid")
                .hasFieldOrPropertyWithValue("name", "Valid")
                .hasFieldOrPropertyWithValue("time", "2019-01-01T00:00:00.000Z")
                .hasFieldOrPropertyWithValue("condition", "Mostly Cold")
                .hasFieldOrPropertyWithValue("temperature", 0)
                .hasFieldOrPropertyWithValue("temperatureUnit", "F");
    }
}
