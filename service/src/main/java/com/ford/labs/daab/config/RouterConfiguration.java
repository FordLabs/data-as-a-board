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

package com.ford.labs.daab.config;

import com.ford.labs.daab.publishers.endpoint.EndpointPublisherHandler;
import com.ford.labs.daab.subscribers.CachedEventSubscriberHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterConfiguration {
    @Bean
    public RouterFunction<ServerResponse> endpointPublisherFunction(EndpointPublisherHandler handler) {
        return route()
                .path("/endpoint", builder -> builder.nest(
                        accept(MediaType.APPLICATION_JSON),
                        builder2 -> builder2
                                .POST("/register", handler::register)
                                .POST("/publish", accept(MediaType.APPLICATION_JSON), handler::publish)
                ))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cachedEventSubscriberFunction(CachedEventSubscriberHandler cachedEventSubscriberHandler, EndpointPublisherHandler endpointPublisherHandler) {
        return route()
                .GET("/event/{id}", cachedEventSubscriberHandler::get)
                .DELETE("/event/{id}", endpointPublisherHandler::delete)
                .build();
    }
}
