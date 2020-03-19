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

package com.ford.labs.daab.radiator;

import com.ford.labs.daab.config.radiator.properties.RadiatorProperties;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RadiatorConfigurationService {
    private final ReactiveRedisTemplate<String, RadiatorProperties> template;

    public RadiatorConfigurationService(ReactiveRedisTemplate<String, RadiatorProperties> template) {
        this.template = template;
    }


    public Mono<RadiatorProperties> getConfiguration() {
        return template.opsForValue().get("radiator");
    }

    public Mono<Void> setConfiguration(RadiatorProperties radiatorProperties) {
        return template.opsForValue()
                .set("radiator", radiatorProperties)
                .then();
    }
}
