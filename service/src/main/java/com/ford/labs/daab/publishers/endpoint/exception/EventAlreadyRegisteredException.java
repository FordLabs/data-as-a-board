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

package com.ford.labs.daab.publishers.endpoint.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EventAlreadyRegisteredException extends ResponseStatusException {
    private static HttpStatus STATUS = HttpStatus.CONFLICT;

    public EventAlreadyRegisteredException() {
        super(STATUS);
    }

    public EventAlreadyRegisteredException(String id) {
        super(STATUS, buildReason(id));
    }

    public EventAlreadyRegisteredException(String id, Throwable cause) {
        super(STATUS, buildReason(id), cause);
    }

    private static String buildReason(String id) {
        return String.format("com.ford.labs.daab.event.Event [%s] is already registered", id);
    }
}
