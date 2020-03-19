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

package com.ford.labs.daab.publishers.endpoint;

import static org.springframework.web.reactive.function.server.ServerResponse.noContent;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

import java.util.List;

import com.ford.labs.daab.publishers.endpoint.model.RegistrationRequest;
import com.ford.labs.daab.publishers.endpoint.model.RegistrationResponse;
import com.ford.labs.daab.publishers.endpoint.validation.PublishEventRequestValidator;
import com.ford.labs.daab.publishers.endpoint.validation.RegistrationRequestValidator;
import com.ford.labs.daab.event.Event;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

@Service
public class EndpointPublisherHandler {
    private EndpointPublisherService service;
    private RegistrationRequestValidator registrationValidator;
    private PublishEventRequestValidator publishEventValidator;

    public EndpointPublisherHandler(EndpointPublisherService service,
                                    RegistrationRequestValidator registrationValidator, PublishEventRequestValidator publishValidator) {
        this.service = service;
        this.registrationValidator = registrationValidator;
        this.publishEventValidator = publishValidator;
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return ok().contentType(MediaType.APPLICATION_JSON)
                .body(request.bodyToMono(RegistrationRequest.class).map(body -> {
                    validate(body, registrationValidator);
                    return body.getId();
                }).flatMap(service::registerEvent).map(RegistrationResponse::new), RegistrationResponse.class);
    }

    public Mono<ServerResponse> publish(ServerRequest request) {
        return request.bodyToMono(Event.class).flatMap(event -> {
            validate(event, publishEventValidator);
            return service.publishEvent(event, getUserKey(request));
        }).flatMap(v -> ok().build());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        return service.deleteEvent(request.pathVariable("id"), getUserKey(request))
                .flatMap(v -> noContent().build());
    }

    private <T> void validate(T request, Validator validator) {
        Errors errors = new BeanPropertyBindingResult(request, ServerRequest.class.getName());
        validator.validate(request, errors);

        if (!errors.getAllErrors().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("The field '%s' is required.", errors.getFieldErrors().get(0).getField())
            );
        }
    }

    private String getUserKey(ServerRequest request) {
        List<String> headerValues = request.headers().header("X-com.ford.labs.daab.event.Event-Key");

        return headerValues.isEmpty() ? null : headerValues.get(0);
    }
}
