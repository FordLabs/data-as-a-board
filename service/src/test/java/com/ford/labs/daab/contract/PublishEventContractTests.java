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

package com.ford.labs.daab.contract;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.ford.labs.daab.publishers.EventPublishingService;
import com.ford.labs.daab.publishers.endpoint.model.RegistrationRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import lombok.Data;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"event.quote.upwise.enabled=false"})
@AutoConfigureWebTestClient
public class PublishEventContractTests {

    @Autowired
    WebTestClient tc;

    @MockBean
    ReactiveRedisTemplate<String, String> template;

    @MockBean
    EventPublishingService eventPublishingService;

    @Mock
    ReactiveHashOperations<String, Object, Object> hashOps;

    @BeforeEach
    public void setup() {
        when(template.opsForHash()).thenReturn(hashOps);
        when(hashOps.hasKey(any(), any())).thenReturn(Mono.just(false));
        when(hashOps.put(any(), any(), any())).thenReturn(Mono.just(true));
        when(hashOps.remove(anyString(), anyString())).thenReturn(Mono.just(1L));
        when(eventPublishingService.publish(any())).thenReturn(Mono.just(1L));
        when(eventPublishingService.delete(any())).thenReturn(Mono.just(1L));
    }

    @Test
    public void registrationWithNullId_GivesBadRequest() throws Exception {
        postTo("/endpoint/register", new RegistrationRequest()).expectStatus().isBadRequest();
    }

    @Test
    public void registrationWithEmptyId_GivesBadRequest() throws Exception {
        postTo("/endpoint/register", new RegistrationRequest("")).expectStatus().isBadRequest();
    }

    @Test
    public void registrationWithValidId_GivesKey() throws Exception {
        postTo("/endpoint/register", new RegistrationRequest("ID")).expectStatus().isOk().expectBody().jsonPath("$.key")
                .isNotEmpty();
    }

    @Test
    public void publishEventWithNullId_GivesBadRequest() throws Exception {
        GenericEvent nullID = event();
        nullID.setId(null);
        postTo("/endpoint/publish", nullID).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithNullType_GivesBadRequest() throws Exception {
        GenericEvent nullType = event();
        nullType.setEventType(null);
        postTo("/endpoint/publish", nullType).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithNullTime_GivesBadRequest() throws Exception {
        GenericEvent nullTime = event();
        nullTime.setTime(null);
        postTo("/endpoint/publish", nullTime).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithNullName_GivesBadRequest() throws Exception {
        GenericEvent nullName = event();
        nullName.setName(null);
        postTo("/endpoint/publish", nullName).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithEmptyId_GivesBadRequest() throws Exception {
        GenericEvent emptyID = event();
        emptyID.setId("");
        postTo("/endpoint/publish", emptyID).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithEmptyType_GivesBadRequest() throws Exception {
        GenericEvent emptyType = event();
        emptyType.setEventType("");
        postTo("/endpoint/publish", emptyType).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithEmptyTime_GivesBadRequest() throws Exception {
        GenericEvent emptyTime = event();
        emptyTime.setTime("");
        postTo("/endpoint/publish", emptyTime).expectStatus().isBadRequest();
    }

    @Test
    public void publishEventWithEmptyName_GivesBadRequest() throws Exception {
        GenericEvent emptyName = event();
        emptyName.setName("");
        postTo("/endpoint/publish", emptyName).expectStatus().isBadRequest();
    }

    @Test
    public void publishValidEvent_Gives200() throws Exception {
        when(hashOps.hasKey(any(), any())).thenReturn(Mono.just(true));
        when(hashOps.get(any(), any())).thenReturn(Mono.just("key"));
        postTo("/endpoint/publish", event(), "key").expectStatus().isOk();
    }

    @Test
    public void publishValidEvent_GivesUnauthorizedWithWrongKey() throws Exception {
        when(hashOps.hasKey(any(), any())).thenReturn(Mono.just(true));
        when(hashOps.get(any(), any())).thenReturn(Mono.just("ValidKey"));
        postTo("/endpoint/publish", event(), "wrongKey").expectStatus().isUnauthorized();
    }

    @Test
    public void deleteEvent_Gives2xxWhenUnregistered() throws Exception {
        deleteTo("/event/test.id").expectStatus().is2xxSuccessful();
    }

    @Test
    public void deleteEvent_GivesUnauthorizedWithWrongKey() throws Exception {
        when(hashOps.hasKey(any(), any())).thenReturn(Mono.just(true));
        when(hashOps.get(any(), any())).thenReturn(Mono.just("ValidKey"));

        deleteTo("/event/test.id", "WrongKey").expectStatus().isUnauthorized();
    }

    @Test
    public void deleteEvent_Gives2xxWithValidKey() throws Exception {
        when(hashOps.hasKey(any(), any())).thenReturn(Mono.just(true));
        when(hashOps.get(any(), any())).thenReturn(Mono.just("ValidKey"));

        deleteTo("/event/test.id", "ValidKey").expectStatus().is2xxSuccessful();
    }

    private GenericEvent event() {
        GenericEvent event = new GenericEvent();
        event.setEventType("QUOTE");
        event.setId("1");
        event.setName("Marvin");
        event.setTime("100100");
        return event;
    }

    private ResponseSpec deleteTo(String endpoint) throws Exception {
        return tc.delete().uri(endpoint).exchange();
    }

    private ResponseSpec deleteTo(String endpoint, String key) throws Exception {
        return tc.delete().uri(endpoint)
                .header("X-com.ford.labs.daab.event.Event-Key", key)
                .exchange();
    }

    private ResponseSpec postTo(String endpoint, Object body) throws Exception {
        return tc.post().uri(endpoint).contentType(MediaType.APPLICATION_JSON).syncBody(body).exchange();
    }

    private ResponseSpec postTo(String endpoint, Object body, String key) throws Exception {
        return tc.post()
                .uri(endpoint)
                .header("X-com.ford.labs.daab.event.Event-Key", key)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(body)
                .exchange();
    }

    @Data
    public class GenericEvent {
        String id;
        String eventType;
        String name;
        String time;
    }
}
