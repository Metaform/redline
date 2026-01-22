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

package com.metaformsystems.redline.infrastructure.client.hashicorpvault;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class HashicorpVaultClientImpl implements HashicorpVaultClient {
    private final WebClient vaultWebClient;
    @Value("${vault.token:root}")
    private String vaultToken;

    public HashicorpVaultClientImpl(WebClient vaultWebClient) {
        this.vaultWebClient = vaultWebClient;
    }

    @Override
    public String readSecret(String path) {
        return vaultWebClient.get()
                .uri(path)
                .header("X-Vault-Token", vaultToken)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.NOT_FOUND), r -> Mono.empty())
                .bodyToMono(VaultGetSecretResponse.class)
                // for some braindead reason it is not allowed to map to null, so we need to map to an empty string...
                .map(r -> r.data() == null ? "" : r.data().getData().get("content"))
                .block();
    }

    private record VaultGetSecretResponse(VaultDataWrapper data) {

        public static class VaultDataWrapper {
            private Map<String, String> data;

            // Getters and Setters
            public Map<String, String> getData() {
                return data;
            }

            public void setData(Map<String, String> data) {
                this.data = data;
            }
        }
    }
}
