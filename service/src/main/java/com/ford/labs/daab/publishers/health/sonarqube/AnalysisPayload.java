package com.ford.labs.daab.publishers.health.sonarqube;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class AnalysisPayload {
    private OffsetDateTime analysedAt;
    private Project project;
    private QualityGate qualityGate;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class Project {
        private String key;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    static class QualityGate {
        private String status;
    }
}
