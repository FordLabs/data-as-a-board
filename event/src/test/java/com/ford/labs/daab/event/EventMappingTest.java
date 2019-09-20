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

package com.ford.labs.daab.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class EventMappingTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void mapsUnknownEventToEventClass() throws IOException {
        String json = "{\"id\": \"unknown.thing\", \"eventType\": \"UNKNOWN\", \"name\": \"Thing\", \"time\": \"2018-01-01T00:00:000Z\"}";
        Event event = mapper.readValue(json, Event.class);

        assertThat(event).isInstanceOf(Event.class);
    }

    @Test
    void mapsJobEventToJobClass() throws IOException {
        String json = "{\"id\": \"job.thing\", \"eventType\": \"JOB\", \"name\": \"Thing\", \"time\": \"2018-01-01T00:00:000Z\", \"status\": \"SUCCESS\"}";
        Event event = mapper.readValue(json, Event.class);

        assertThat(event).isInstanceOf(JobEvent.class);
    }

    @Test
    void mapsFigureEventToFigureClass() throws IOException {
        String json = "{\"id\": \"job.thing\", \"eventType\": \"FIGURE\", \"name\": \"Thing\", \"time\": \"2018-01-01T00:00:000Z\", \"value\": \"12\", \"subtext\": \"daily active users\"}";
        Event event = mapper.readValue(json, Event.class);

        assertThat(event).isInstanceOf(FigureEvent.class);
    }

    @Test
    void mapsHealthEventToHealthClass() throws IOException {
        String json = "{\"id\": \"job.thing\", \"eventType\": \"HEALTH\", \"name\": \"Thing\", \"time\": \"2018-01-01T00:00:000Z\", \"status\": \"UP\"}";
        Event event = mapper.readValue(json, Event.class);

        assertThat(event).isInstanceOf(HealthEvent.class);
    }

    @Test
    void mapsPercentageEventToPercentageClass() throws IOException {
        String json = "{\"id\": \"job.thing\", \"eventType\": \"PERCENTAGE\", \"name\": \"Thing\", \"time\": \"2018-01-01T00:00:000Z\", \"value\": 0.9876}";
        Event event = mapper.readValue(json, Event.class);

        assertThat(event).isInstanceOf(PercentageEvent.class);
    }
}