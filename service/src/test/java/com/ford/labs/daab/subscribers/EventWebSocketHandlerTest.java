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

package com.ford.labs.daab.subscribers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.labs.daab.event.HealthEvent;
import com.ford.labs.daab.event.JobEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class EventWebSocketHandlerTest {
    private EventWebSocketHandler subject;
    private EventSubscriptionService mockEventSubscriptionService = mock(EventSubscriptionService.class);
    private ObjectMapper mockMapper = mock(ObjectMapper.class);

    @BeforeEach
    void setup() {
        subject = new EventWebSocketHandler(mockEventSubscriptionService, mockMapper);
    }

    @Test
    void handle_publishesEventsFromCacheAndQueueToWebSocketSession() throws JsonProcessingException {
        JobEvent cachedEvent = new JobEvent();
        HealthEvent freshEvent = new HealthEvent();

        when(mockMapper.writeValueAsString(eq(cachedEvent))).thenReturn("CACHED EVENT");
        when(mockMapper.writeValueAsString(eq(freshEvent))).thenReturn("FRESH EVENT");

        WebSocketSession session = mock(WebSocketSession.class);
        when(session.send(any())).thenReturn(Mono.empty());
        when(session.textMessage(any())).thenAnswer(i -> stringToWebSocketMessage(i.getArgument(0).toString()));

        when(mockEventSubscriptionService.getCachedEvents()).thenReturn(Flux.just(cachedEvent));
        when(mockEventSubscriptionService.subscribe(anyString())).thenReturn(Flux.just(freshEvent));

        subject.handle(session).block();

        ArgumentCaptor<Publisher<WebSocketMessage>> messageCaptor = ArgumentCaptor.forClass(Publisher.class);

        verify(session).send(messageCaptor.capture());

        StepVerifier.create(messageCaptor.getValue())
                .assertNext(message -> assertThat(message.getPayloadAsText()).isEqualTo("WEB SOCKET MESSAGE:CACHED EVENT"))
                .assertNext(message -> assertThat(message.getPayloadAsText()).isEqualTo("WEB SOCKET MESSAGE:FRESH EVENT"))
                .verifyComplete();
    }

    private WebSocketMessage stringToWebSocketMessage(String rawMessage) {
        return new WebSocketMessage(
                WebSocketMessage.Type.TEXT,
                stringToDataBuffer(rawMessage)
        );
    }

    private DataBuffer stringToDataBuffer(String rawMessage) {
        return new DefaultDataBufferFactory(true)
                .wrap(String.join(":", "WEB SOCKET MESSAGE", rawMessage).getBytes());
    }

}