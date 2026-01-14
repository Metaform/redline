package com.metaformsystems.redline.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class TokenProviderImpl implements TokenProvider {

    private final WebClient keycloakTokenClient;

    public TokenProviderImpl(WebClient keycloakTokenClient) {
        this.keycloakTokenClient = keycloakTokenClient;
    }

    @Override
    public String getToken(String clientId, String clientSecret, String scopes) {
        var tokenResponse = keycloakTokenClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret + "&scope=" + scopes)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();

        return tokenResponse.accessToken();
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
