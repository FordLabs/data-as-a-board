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

package com.ford.labs.daab;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class WireMockExtension implements BeforeEachCallback, AfterEachCallback {
    private WireMockServer server;

    public static final String WIREMOCK_URL = "http://localhost:8123";

    public WireMockExtension() {
        this(8123);
    }

    public WireMockExtension(int port) {
        server = new WireMockServer(
                WireMockConfiguration.options()
                        .port(port)
        );
    }

    public WireMockServer getServer() {
        return server;
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        server.start();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        server.stop();
    }
}
