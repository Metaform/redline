package com.metaformsystems.redline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class HashicorpVaultConfig {
    @Value("${vault.url:http://vault.localhost}")
    private String vaultUrl;

    @Bean
    public WebClient vaultWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(vaultUrl)
                .build();
    }
}
