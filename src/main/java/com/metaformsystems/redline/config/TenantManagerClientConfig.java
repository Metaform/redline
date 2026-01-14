package com.metaformsystems.redline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class TenantManagerClientConfig {

    @Value("${tenant-manager.url:http://tm.localhost}")
    private String tenantManagerUrl;

    @Bean
    public WebClient tenantManagerWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(tenantManagerUrl)
                .build();
    }
}
