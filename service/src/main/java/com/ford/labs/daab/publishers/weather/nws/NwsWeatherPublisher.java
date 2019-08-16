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

package com.ford.labs.daab.publishers.weather.nws;

import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.weather.NwsWeatherProperties;
import com.ford.labs.daab.config.event.properties.weather.WeatherProperties;
import com.ford.labs.daab.model.event.WeatherEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class NwsWeatherPublisher {
    private static final Logger log = getLogger(NwsWeatherPublisher.class);
    private final EventPublishingService service;
    private final WebClient client;

    private final EventProperties eventProperties;

    public NwsWeatherPublisher(
            EventPublishingService service,
            WebClient client,
            EventProperties eventProperties,
            @Value("${nws.url:https://api.weather.gov}") String nwsUrl
    ) {
        this.service = service;
        this.client = client.mutate()
                .baseUrl(nwsUrl)
                .build();
        this.eventProperties = eventProperties;
    }

    @Scheduled(fixedRate = 600000)
    public void pollWeather() {
        Flux.fromIterable(getWeatherLocations())
                .flatMap(location -> this.getPointsResponse(location)
                        .map(this::pointsResponseToForecastUrl)
                        .flatMap(this::getForecastResponse)
                        .filter(Objects::nonNull)
                        .map(response -> {
                            var currentPeriod = response.getProperties().getPeriods().get(0);

                            var event = new WeatherEvent();
                            event.setId(String.format("weather.%s", location.getId()));
                            event.setName(location.getName());
                            event.setEventType("WEATHER");
                            event.setTime(response.getProperties().getUpdated());
                            event.setTemperature(currentPeriod.getTemperature());
                            event.setTemperatureUnit(currentPeriod.getTemperatureUnit());
                            event.setCondition(currentPeriod.getShortForecast());
                            return event;
                        })
                        .flatMap(service::publish)
                        .onErrorReturn(0L)
                )
                .blockLast();
    }

    private List<NwsWeatherProperties> getWeatherLocations() {
        return Optional.of(this.eventProperties)
                .map(EventProperties::getWeather)
                .map(WeatherProperties::getNws)
                .orElse(emptyList());
    }

    private Mono<NwsPointsResponse> getPointsResponse(NwsWeatherProperties location) {
        var uri = String.format("/points/%s,%s", location.getLat(), location.getLon());

        return client.get()
                .uri(uri)
                .header("Host", "api.weather.gov")
                .retrieve()
                .bodyToMono(NwsPointsResponse.class)
                .doOnError(error -> log.error("Error fetching Points Response: ", error));
    }

    private String pointsResponseToForecastUrl(NwsPointsResponse response) {
        return response.getProperties().getForecast();
    }

    private Mono<NwsForecastResponse> getForecastResponse(String uri) {
        return client.get()
                .uri(uri)
                .header("Host", "api.weather.gov")
                .retrieve()
                .bodyToMono(NwsForecastResponse.class)
                .doOnError(error -> log.error("Error fetching Forecast Response: ", error));
    }

}
