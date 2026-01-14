package com.metaformsystems.redline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class IdentityHubConfig {
    @Value("${identityhub.url:http://ih.localhost/cs}")
    private String identityHubUrl;

    @Bean
    public WebClient identityHubWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(identityHubUrl)
                .build();
    }
}
