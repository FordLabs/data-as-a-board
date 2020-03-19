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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RadiatorConfigurationServiceTest {
    private ReactiveRedisTemplate<String, RadiatorProperties> mockTemplate = mock(ReactiveRedisTemplate.class);
    private ReactiveValueOperations<String, RadiatorProperties> mockOpsForValue = mock(ReactiveValueOperations.class);

    private RadiatorConfigurationService subject = new RadiatorConfigurationService(mockTemplate);

    @BeforeEach
    public void setUpTemplate() {
        when(mockTemplate.opsForValue()).thenReturn(mockOpsForValue);
        when(mockOpsForValue.set(any(),any())).thenReturn(Mono.just(true));
    }

    @Test
    public void getConfiguration_returnsConfiguration() {
        var expectedRadiatorProperties = new RadiatorProperties();
        expectedRadiatorProperties.setBackground("http://localhost:2222/image.png");


        when(mockOpsForValue.get("radiator")).thenReturn(Mono.just(expectedRadiatorProperties));

        subject.getConfiguration()
                .as(StepVerifier::create)
                .expectNext(expectedRadiatorProperties)
                .verifyComplete();
    }

    @Test
    public void setConfiguration_setsConfiguration() {
        var expectedRadiatorProperties = new RadiatorProperties();
        expectedRadiatorProperties.setBackground("http://localhost:2222/image.png");

        subject.setConfiguration(expectedRadiatorProperties)
                .as(StepVerifier::create)
                .verifyComplete();

        verify(mockOpsForValue).set("radiator",expectedRadiatorProperties);
    }

}