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

package com.ford.labs.daab.model.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType", defaultImpl = Event.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = JobEvent.class, name = EventType.JOB),
        @JsonSubTypes.Type(value = FigureEvent.class, name = EventType.FIGURE),
        @JsonSubTypes.Type(value = QuoteEvent.class, name = EventType.QUOTE),
        @JsonSubTypes.Type(value = HealthEvent.class, name = EventType.HEALTH),
        @JsonSubTypes.Type(value = PercentageEvent.class, name = EventType.PERCENTAGE),
        @JsonSubTypes.Type(value = StatisticsEvent.class, name = EventType.STATISTICS),
        @JsonSubTypes.Type(value = WeatherEvent.class, name = EventType.WEATHER),
        @JsonSubTypes.Type(value = ListEvent.class, name = EventType.LIST),
        @JsonSubTypes.Type(value = ImageEvent.class, name = EventType.IMAGE)
})
public class Event {
    String id;
    @JsonIgnore String eventType;
    EventLevel level = EventLevel.OK;
    String name;
    String time = OffsetDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
}
