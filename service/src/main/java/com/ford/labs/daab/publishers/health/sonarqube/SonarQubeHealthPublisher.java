package com.ford.labs.daab.publishers.health.sonarqube;

import com.ford.labs.daab.event.HealthEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;

@RestController
public class SonarQubeHealthPublisher {
    private final EventPublishingService eventPublishingService;

    public SonarQubeHealthPublisher(EventPublishingService eventPublishingService) {
        this.eventPublishingService = eventPublishingService;
    }

    @PostMapping("/api/sonarqube/webhook")
    public Mono<Long> receiveAnalysis(@RequestBody AnalysisPayload payload) {
        HealthEvent healthEvent = new HealthEvent();

        healthEvent.setId(String.format("health.sonarqube.%s", payload.getProject().getKey()));
        healthEvent.setName(payload.getProject().getName());
        healthEvent.setTime(payload.getAnalysedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        healthEvent.setStatus(healthStatusFromSonarQubeStatus(payload.getQualityGate().getStatus()));

        return this.eventPublishingService
                .publish(healthEvent);
    }

    private static HealthEvent.Status healthStatusFromSonarQubeStatus(String status) {
        switch (status) {
            case "OK":
                return HealthEvent.Status.UP;
            case "ERROR":
                return HealthEvent.Status.DOWN;
            default:
                return HealthEvent.Status.UNKNOWN;
        }
    }
}
