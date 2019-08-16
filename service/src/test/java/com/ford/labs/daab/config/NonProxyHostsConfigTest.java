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

package com.ford.labs.daab.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test class does not test any functionality in DaaB.
 * It exists to test the recommended NO_PROXY environment variable for DaaB usage
 * within the Ford firewall.
 */
public class NonProxyHostsConfigTest {
    private Pattern pattern;

    @BeforeEach
    public void setup() {
        pattern = new WebClientConfiguration(
                "",
                "localhost,127.0.0.1,19.0.0.0/8,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16,sumalab.ford.com,.fsillab.ford.com,.fsillab.ford.com,www.gsoutils.ford.com,.hplab1.ford.com,.hplab1.ford.com,10.3.0.0/16,10.2.0.0/16,.cluster.local,.edc1.cf.ford.com,.caas.ford.com"
        ).nonProxyHostsMatcher();
    }

    @Test
    public void localhost_matches() {
        assertThat("http://localhost:8080")
                .matches(pattern);
    }

    @Test
    public void pcfEdc1PreProd_matches() {
        assertThat("https://application.apps.pp01i.edc1.cf.ford.com/api/endpoint")
                .matches(pattern);
    }

    @Test
    public void pcfEdc1Prod_matches() {
        assertThat("https://application.apps.pd01i.edc1.cf.ford.com/api/endpoint")
                .matches(pattern);
    }

    @Test
    public void pcfAzureDev_doesNotMatch() {
        assertThat("https://application.apps.cl-east-dev02.cf.ford.com/")
                .doesNotMatch(pattern);
    }

    @Test
    public void pcfAzureProd_doesNotMatch() {
        assertThat("https://application.apps.cl-east-prod01.cf.ford.com/")
                .doesNotMatch(pattern);
    }

    @Test
    public void caas_matches() {
        assertThat("https://application.type.app.caas.ford.com/")
                .matches(pattern);
    }

    @Test
    public void retroquest_doesNotMatch() {
        assertThat("https://retroquest.ford.com/api")
                .doesNotMatch(pattern);
    }

    @Test
    public void hourOfPower_matches() {
        assertThat("https://hourofpower.apps.pp01i.edc1.cf.ford.com/api/topic/next/3")
                .matches(pattern);
    }

    @Test
    public void upwise_doesNotMatch() {
        assertThat("https://upwise.cfapps.io/api")
                .doesNotMatch(pattern);
    }

    @Test
    public void hockeyapp_doesNotMatch() {
        assertThat("https://rink.hockeyapp.net/api")
                .doesNotMatch(pattern);
    }
}
