package com.metaformsystems.redline.client.hashicorpvault;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class HashicorpVaultClientImpl implements HashicorpVaultClient {
    private final WebClient vaultWebClient;

    public HashicorpVaultClientImpl(WebClient vaultWebClient) {
        this.vaultWebClient = vaultWebClient;
    }

    @Override
    public String readSecret(String path) {
        return vaultWebClient.get()
                .uri(path)
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
