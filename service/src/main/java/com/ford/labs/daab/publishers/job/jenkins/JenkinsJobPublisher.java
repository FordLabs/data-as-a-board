/*
 * Copyright (c) 2019 Ford Motor Company
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

package com.ford.labs.daab.publishers.job.jenkins;

import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.job.JenkinsJob;
import com.ford.labs.daab.config.event.properties.job.JenkinsJobProperties;
import com.ford.labs.daab.config.event.properties.job.JobProperties;
import com.ford.labs.daab.model.event.JobEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;

@Service
public class JenkinsJobPublisher {
    private final EventPublishingService eventPublishingService;
    private final WebClient client;
    private final EventProperties eventProperties;

    public JenkinsJobPublisher(
            EventPublishingService eventPublishingService,
            WebClient client,
            EventProperties eventProperties) {
        this.eventPublishingService = eventPublishingService;
        this.client = client;
        this.eventProperties = eventProperties;
    }

    @Scheduled(fixedRate = 30000)
    public void pollJobs() {
        for (var job : getJenkinsJobs()) {
            makeRequest(job)
                    .flatMap(response -> buildJenkinsJobEvent(job, response))
                    .onErrorResume(error -> {
                        var event = new JobEvent();
                        event.setId("job.jenkins." + job.getId());
                        event.setName(job.getName());
                        event.setStatus(JobEvent.Status.UNKNOWN);
                        event.setTime(null);
                        return Mono.just(event);
                    })
                    .flatMap(eventPublishingService::publish)
                    .block();
        }
    }

    private Mono<JenkinsJobResponse> makeRequest(JenkinsJob job) {
        return client.get()
                .uri(job.getUrl())
                .header("Authorization", buildBasicAuthHeader())
                .retrieve()
                .bodyToMono(JenkinsJobResponse.class);
    }

    private Mono<JenkinsBuildResponse> makeBuildRequest(String url) {
        return client.get()
                .uri(url)
                .header("Authorization", buildBasicAuthHeader())
                .retrieve()
                .bodyToMono(JenkinsBuildResponse.class);
    }

    private String buildBasicAuthHeader() {
        return String.format(
                "Basic %s",
                Base64Utils.encodeToString((String.format(
                        "%s:%s",
                        this.eventProperties.getJob().getJenkins().getUsername(),
                        this.eventProperties.getJob().getJenkins().getToken())).getBytes(UTF_8)
                )
        );
    }

    private List<JenkinsJob> getJenkinsJobs() {
        return Optional.of(this.eventProperties)
                .map(EventProperties::getJob)
                .map(JobProperties::getJenkins)
                .map(JenkinsJobProperties::getJobs)
                .orElse(emptyList());
    }

    private Mono<JobEvent> buildJenkinsJobEvent(JenkinsJob job, JenkinsJobResponse jenkinsJobResponse) {
        var event = new JobEvent();
        event.setId("job.jenkins." + job.getId());
        event.setName(job.getName());
        event.setStatus(getBuildStatus(jenkinsJobResponse));

        if (jenkinsJobResponse.getBuilds().size() == 0) {
            return Mono.just(event);
        }

        String lastBuildUrl = jenkinsJobResponse.getBuilds().get(0).getUrl();
        event.setUrl(lastBuildUrl);

        return getBuildTime(lastBuildUrl)
                .map(time -> time.format(DateTimeFormatter.ISO_DATE_TIME))
                .map(formattedTime -> {
                    event.setTime(formattedTime);
                    return event;
                });
    }

    private Mono<OffsetDateTime> getBuildTime(String lastBuildUrl) {
        return makeBuildRequest(String.format("%sapi/json", lastBuildUrl))
                .map(JenkinsBuildResponse::getTimestamp)
                .map(Instant::ofEpochMilli)
                .map(instant -> instant.atOffset(ZoneOffset.UTC));
    }

    private JobEvent.Status getBuildStatus(JenkinsJobResponse jenkinsJobResponse) {
        if (jenkinsJobResponse.getColor().equals("disabled")) {
            return JobEvent.Status.DISABLED;
        }
        if (jenkinsJobResponse.getColor().endsWith("_anime")) {
            return JobEvent.Status.IN_PROGRESS;
        }
        if (jenkinsJobResponse.getColor().equals("blue")) {
            return JobEvent.Status.SUCCESS;
        }
        return JobEvent.Status.FAILURE;
    }
}
