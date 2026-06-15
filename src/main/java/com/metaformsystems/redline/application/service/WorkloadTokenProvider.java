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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.util.Optional.ofNullable;

/**
 * This provider reads a workload token from a file share, provided by Kubernetes, and exchanges it for a scoped token
 * using the Token Exchange Protocol (RFC 8693 - OAuth 2.0 Token Exchange).
 */
@Component("token-exchange")
public class WorkloadTokenProvider implements TokenProvider {
    private final String tokenFilePath;
    private final String tokenExchangeAudience;
    private final String defaultResource;
    private final WebClient webClient;

    public WorkloadTokenProvider(@Value("${token.file.path:/var/run/secrets/jwtlet/token}") String tokenFilePath,
                                 @Value("${token.exchange.audience:edcv}") String tokenExchangeAudience,
                                 @Value("${token.exchange.resource:redline}") String defaultResource,
                                 @Qualifier("tokenExchangeClient") WebClient webClient) {
        this.tokenFilePath = tokenFilePath;
        this.tokenExchangeAudience = tokenExchangeAudience;
        this.defaultResource = defaultResource;
        this.webClient = webClient;
    }

    @Override
    public String getToken(String resource, String scopes) {
        try {
            var tokenContent = Files.readString(Path.of(tokenFilePath));

            var response = webClient.post()
                    .uri("/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange")
                            .with("subject_token", tokenContent)
                            .with("audience", tokenExchangeAudience)
                            .with("resource", ofNullable(resource).orElse(defaultResource))
                            .with("scope", scopes))
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();

            return Objects.requireNonNull(response).accessToken();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record TokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
