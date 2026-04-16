package com.metaformsystems.redline.infrastructure.client.siglet;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
public class SigletApiClientImpl implements SigletApiClient{

    private final WebClient sigletWebClient;

    public SigletApiClientImpl(WebClient sigletWebClient) {
        this.sigletWebClient = sigletWebClient;
    }

    @Override
    public Map<String, Object> getDataAddress(String participantContextId, String transferProcessId) {
        return sigletWebClient.get()
                .uri("/tokens/{participantContextId}/{transferProcessId}", participantContextId, transferProcessId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
    }
}
