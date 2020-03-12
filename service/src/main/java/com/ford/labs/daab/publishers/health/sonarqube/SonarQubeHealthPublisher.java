package com.ford.labs.daab.publishers.health.sonarqube;

import com.ford.labs.daab.event.EventLevel;
import com.ford.labs.daab.event.StatusEvent;
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
        StatusEvent event = new StatusEvent();

        event.setId(String.format("status.sonarqube.%s", payload.getProject().getKey()));
        event.setName(payload.getProject().getName());
        event.setTime(payload.getAnalysedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        event.setLevel(eventLevelFromSonarQubeStatus(payload.getQualityGate().getStatus()));
        event.setStatusText(statusTextFromSonarQubeStatus(payload.getQualityGate().getStatus()));

        return this.eventPublishingService
                .publish(event);
    }

    private static EventLevel eventLevelFromSonarQubeStatus(String status) {
        switch (status) {
            case "OK":
                return EventLevel.OK;
            case "ERROR":
                return EventLevel.ERROR;
            default:
                return EventLevel.UNKNOWN;
        }
    }

    private static String statusTextFromSonarQubeStatus(String status) {
        switch (status) {
            case "OK":
                return "Passed";
            case "ERROR":
                return "Failed";
            default:
                return "Unknown";
        }
    }
}
