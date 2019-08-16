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

package com.ford.labs.daab.subscribers.slack;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static java.util.Collections.singletonList;

@Component
public class SlackClient {
    private final String slackUrl;
    private final WebClient client;

    public SlackClient(
            WebClient client,
            @Value("${slack.url:https://slack.com/api}") String slackUrl

    ) {
        this.client = client;
        this.slackUrl = slackUrl;
    }

    public Mono<PostMessageResponse> postMessage(PostMessageRequest request, String token) {
        return client.post()
                .uri(String.format("%s/chat.postMessage", slackUrl))
                .syncBody(request)
                .header("Authorization", String.format("Bearer %s", token))
                .retrieve()
                .bodyToMono(PostMessageResponse.class);
    }
}
