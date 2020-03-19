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

package com.ford.labs.daab.publishers.health.sonarqube;

import com.ford.labs.daab.event.EventLevel;
import com.ford.labs.daab.event.StatusEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SonarQubeHealthPublisherTest {
    private EventPublishingService mockEventPublishingService;
    private SonarQubeHealthPublisher subject;

    public SonarQubeHealthPublisherTest() {
        mockEventPublishingService = mock(EventPublishingService.class);
        this.subject = new SonarQubeHealthPublisher(mockEventPublishingService);

        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
    }

    @Test
    public void receiveAnalysis_whenOK_publishesNewHealthEventWithGoodStatus() {
        OffsetDateTime now = OffsetDateTime.now();
        AnalysisPayload payload = AnalysisPayload.builder()
                .analysedAt(now)
                .project(AnalysisPayload.Project.builder()
                        .key("test.id")
                        .name("Test Name")
                        .build()
                )
                .qualityGate(AnalysisPayload.QualityGate.builder()
                        .status("OK")
                        .build()
                )
                .build();

        subject.receiveAnalysis(payload)
                .block();

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("status.sonarqube.test.id")
                                && event.getName().equals("Test Name")
                                && event.getLevel().equals(EventLevel.OK)
                                && ((StatusEvent) event).getStatusText().equals("Passed")
                ));
    }

    @Test
    public void receiveAnalysis_whenError_publishesNewHealthEventWithBadStatus() {
        OffsetDateTime now = OffsetDateTime.now();
        AnalysisPayload payload = AnalysisPayload.builder()
                .analysedAt(now)
                .project(AnalysisPayload.Project.builder()
                        .key("test.id")
                        .name("Test Name")
                        .build()
                )
                .qualityGate(AnalysisPayload.QualityGate.builder()
                        .status("ERROR")
                        .build()
                )
                .build();

        subject.receiveAnalysis(payload)
                .block();

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("status.sonarqube.test.id")
                                && event.getName().equals("Test Name")
                                && event.getLevel().equals(EventLevel.ERROR)
                                && ((StatusEvent) event).getStatusText().equals("Failed")
                ));
    }

    @Test
    public void receiveAnalysis_whenAnythingElse_publishesNewHealthEventWithUnknownStatus() {
        OffsetDateTime now = OffsetDateTime.now();
        AnalysisPayload payload = AnalysisPayload.builder()
                .analysedAt(now)
                .project(AnalysisPayload.Project.builder()
                        .key("test.id")
                        .name("Test Name")
                        .build()
                )
                .qualityGate(AnalysisPayload.QualityGate.builder()
                        .status("asdf")
                        .build()
                )
                .build();

        subject.receiveAnalysis(payload)
                .block();

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("status.sonarqube.test.id")
                                && event.getName().equals("Test Name")
                                && event.getLevel().equals(EventLevel.UNKNOWN)
                                && ((StatusEvent) event).getStatusText().equals("Unknown")
                ));
    }
}
