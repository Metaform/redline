package com.metaformsystems.redline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KeycloakConfig {
    @Value("${keycloak.tokenurl:http://keycloak.localhost/realms/edcv/protocol/openid-connect/token}")
    private String keycloakTokenUrl;

    @Bean
    public WebClient keycloakTokenClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(keycloakTokenUrl)
                .build();
    }
}
