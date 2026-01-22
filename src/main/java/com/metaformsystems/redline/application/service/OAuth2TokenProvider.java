/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package com.metaformsystems.redline.application.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OAuth2TokenProvider implements TokenProvider {

    private final WebClient keycloakTokenClient;

    public OAuth2TokenProvider(WebClient keycloakTokenClient) {
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
