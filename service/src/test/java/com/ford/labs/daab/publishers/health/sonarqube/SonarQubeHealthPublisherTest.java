package com.ford.labs.daab.publishers.health.sonarqube;

import com.ford.labs.daab.event.HealthEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
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
                        event.getId().equals("health.sonarqube.test.id")
                                && event.getName().equals("Test Name")
                                && ((HealthEvent) event).getStatus().equals(HealthEvent.Status.UP)
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
                        event.getId().equals("health.sonarqube.test.id")
                                && event.getName().equals("Test Name")
                                && ((HealthEvent) event).getStatus().equals(HealthEvent.Status.DOWN)
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
                        event.getId().equals("health.sonarqube.test.id")
                                && event.getName().equals("Test Name")
                                && ((HealthEvent) event).getStatus().equals(HealthEvent.Status.UNKNOWN)
                ));
    }
}
