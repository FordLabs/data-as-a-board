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

import com.ford.labs.daab.WireMockExtension;
import com.ford.labs.daab.config.event.properties.EventProperties;
import com.ford.labs.daab.config.event.properties.job.JenkinsJob;
import com.ford.labs.daab.config.event.properties.job.JenkinsJobProperties;
import com.ford.labs.daab.config.event.properties.job.JobProperties;
import com.ford.labs.daab.event.JobEvent;
import com.ford.labs.daab.publishers.EventPublishingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
class JenkinsJobPublisherTest {

    @RegisterExtension
    static WireMockExtension wireMock = new WireMockExtension();

    EventPublishingService mockEventPublishingService = mock(EventPublishingService.class);
    EventProperties eventProperties = new EventProperties();
    JenkinsJobProperties jenkinsJobProperties = new JenkinsJobProperties();

    JenkinsJobPublisher subject = new JenkinsJobPublisher(
            mockEventPublishingService,
            WebClient.create(),
            eventProperties
    );

    @BeforeEach
    void setup() {
        JobProperties jobProperties = new JobProperties();
        jobProperties.setJenkins(jenkinsJobProperties);
        eventProperties.setJob(jobProperties);

        when(mockEventPublishingService.publish(any())).thenReturn(Mono.just(1L));
    }

    @Test
    void pollJobs_getsJobStatusAndPublishesEvent() {
        List<JenkinsJob> jobs = asList(
                new JenkinsJob("success", "Success", "http://localhost:8123/job/success"),
                new JenkinsJob("failure", "Failure", "http://localhost:8123/job/failure"),
                new JenkinsJob("inprogress", "In Progress", "http://localhost:8123/job/in-progress"),
                new JenkinsJob("disabled", "Disabled", "http://localhost:8123/job/disabled"),
                new JenkinsJob("unknown", "Unknown", "http://localhost:8123/job/unknown")
        );

        jenkinsJobProperties.setJobs(jobs);

        wireMock.getServer().stubFor(get(urlEqualTo("/job/success"))
                .willReturn(okJson("{\"color\": \"blue\", \"builds\": [{\"url\": \"http://localhost:8123/job/success/1/\"}]}"))
        );
        wireMock.getServer().stubFor(get(urlEqualTo("/job/success/1/api/json"))
                .willReturn(okJson("{\"timestamp\": 11231}"))
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/job/failure"))
                .willReturn(okJson("{\"color\": \"red\", \"builds\": [{\"url\": \"http://localhost:8123/job/failure/4/\"}]}"))
        );
        wireMock.getServer().stubFor(get(urlEqualTo("/job/failure/4/api/json"))
                .willReturn(okJson("{\"timestamp\": 11233}"))
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/job/in-progress"))
                .willReturn(okJson("{\"color\": \"red_anime\", \"builds\": [{\"url\": \"http://localhost:8123/job/in-progress/12/\"}]}"))
        );
        wireMock.getServer().stubFor(get(urlEqualTo("/job/in-progress/12/api/json"))
                .willReturn(okJson("{\"timestamp\": 11235}"))
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/job/disabled"))
                .willReturn(okJson("{\"color\": \"disabled\", \"builds\": [{\"url\": \"http://localhost:8123/job/disabled/48/\"}]}"))
        );
        wireMock.getServer().stubFor(get(urlEqualTo("/job/disabled/48/api/json"))
                .willReturn(okJson("{\"timestamp\": 11238}"))
        );

        wireMock.getServer().stubFor(get(urlEqualTo("/job/unknown"))
                .willReturn(notFound())
        );

        subject.pollJobs();

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("job.jenkins.success")
                                && event.getName().equals("Success")
                                && ((JobEvent) event).getStatus().equals(JobEvent.Status.SUCCESS)
                                && ((JobEvent) event).getUrl().equals("http://localhost:8123/job/success/1/")
                                && event.getTime().equals(Instant.ofEpochMilli(11231L).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                ));

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("job.jenkins.failure")
                                && event.getName().equals("Failure")
                                && ((JobEvent) event).getStatus().equals(JobEvent.Status.FAILURE)
                                && ((JobEvent) event).getUrl().equals("http://localhost:8123/job/failure/4/")
                                && event.getTime().equals(Instant.ofEpochMilli(11233L).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                ));

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("job.jenkins.inprogress")
                                && event.getName().equals("In Progress")
                                && ((JobEvent) event).getStatus().equals(JobEvent.Status.IN_PROGRESS)
                                && ((JobEvent) event).getUrl().equals("http://localhost:8123/job/in-progress/12/")
                                && event.getTime().equals(Instant.ofEpochMilli(11235L).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                ));

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("job.jenkins.disabled")
                                && event.getName().equals("Disabled")
                                && ((JobEvent) event).getStatus().equals(JobEvent.Status.DISABLED)
                                && ((JobEvent) event).getUrl().equals("http://localhost:8123/job/disabled/48/")
                                && event.getTime().equals(Instant.ofEpochMilli(11238L).atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME))
                ));

        verify(mockEventPublishingService)
                .publish(argThat(event ->
                        event.getId().equals("job.jenkins.unknown")
                                && event.getName().equals("Unknown")
                                && ((JobEvent) event).getStatus().equals(JobEvent.Status.UNKNOWN)
                                && event.getTime() == null
                ));
    }
}
