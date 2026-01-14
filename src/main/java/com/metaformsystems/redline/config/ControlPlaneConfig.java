package com.metaformsystems.redline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ControlPlaneConfig {
    @Value("${controlplane.url:http://cp.localhost/api/mgmt/v4alpha}")
    private String controlPlaneUrl;

    @Bean
    public WebClient controlPlaneWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(controlPlaneUrl)
                .build();
    }
}
