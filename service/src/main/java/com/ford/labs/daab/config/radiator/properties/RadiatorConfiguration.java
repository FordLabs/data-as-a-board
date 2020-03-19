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

package com.ford.labs.daab.config.radiator.properties;

import com.ford.labs.daab.radiator.RadiatorConfigurationService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;

@Configuration
@Profile({"!test"})
@EnableConfigurationProperties(RadiatorProperties.class)
public class RadiatorConfiguration {
    private RadiatorProperties radiatorProperties;
    private RadiatorConfigurationService service;

    public RadiatorConfiguration(RadiatorProperties radiatorProperties, RadiatorConfigurationService service) {
        this.radiatorProperties = radiatorProperties;
        this.service = service;
    }

    @PostConstruct
    public void updatePropertiesIfMissing() {
        service.getConfiguration()
                .defaultIfEmpty(radiatorProperties)
                .flatMap(service::setConfiguration)
                .block();
    }
}
