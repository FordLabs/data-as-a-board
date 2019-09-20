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

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class CachedEventSubscriberHandler {
    private EventSubscriptionService service;

    public CachedEventSubscriberHandler(EventSubscriptionService service) {
        this.service = service;
    }

    public Mono<ServerResponse> get(ServerRequest request) {
        return service.getCachedEventOrEmpty(request.pathVariable("id"))
                .flatMap(event -> ServerResponse.ok().syncBody(event))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
