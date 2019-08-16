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

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

@Configuration
public class WebClientConfiguration {
    private static final Logger log = getLogger(WebClientConfiguration.class);
    private final String httpProxy;
    private String noProxyCommaSeparated;

    public WebClientConfiguration(
            @Value("${https.proxy:}") String httpProxy, @Value("${no.proxy:}") String noProxyCommaSeparated) {
        this.httpProxy = httpProxy;
        this.noProxyCommaSeparated = noProxyCommaSeparated;
    }

    public Pattern nonProxyHostsMatcher() {
        return Pattern.compile(
                ".*(" +
                        noProxyCommaSeparated
                                .replace("*", ".*")
                                .replace(",", "|")
                                .replace(".", "\\.")
                                .replace("/", "\\/") +
                        ").*");
    }

    @Bean
    @Primary
    public HttpClient client() throws SSLException {
        var sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        return HttpClient.create()
                .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext))
                .tcpConfiguration(this::addProxyToTcpClient)
                .followRedirect(true);
    }

    @Bean
    public ClientHttpConnector connector(HttpClient client) {
        return new ReactorClientHttpConnector(client);
    }

    @Bean(name = "proxyWebClient")
    public WebClient proxyWebClient(ClientHttpConnector connector) {

        if (StringUtils.hasText(httpProxy)) {
            log.info(String.format("Using proxy from \"https.proxy\" configuration value: [%s]", httpProxy));
        } else {
            log.info("\"http.proxy\" configuration value not set. No proxy will be used. ");
        }

        return WebClient.builder()
                .clientConnector(connector)
                .build();
    }

    private TcpClient addProxyToTcpClient(TcpClient client) {
        return StringUtils.hasText(httpProxy)
                ? client.proxy(this::proxyConfig)
                : client.noProxy();
    }

    private ProxyProvider proxyConfig(ProxyProvider.TypeSpec proxy) {
        try {
            var proxyUri = new URI(httpProxy.startsWith("http") ? httpProxy : "http://" + httpProxy);

            return proxy.type(ProxyProvider.Proxy.HTTP)
                    .host(proxyUri.getHost())
                    .nonProxyHosts(nonProxyHostsMatcher().pattern())
                    .port(proxyUri.getPort())
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(String.format("The \"https.proxy\" proxy configuration value [%s] is invalid. ", httpProxy), e);
        }
    }
}
