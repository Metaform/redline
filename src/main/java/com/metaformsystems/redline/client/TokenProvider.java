package com.metaformsystems.redline.client;

public interface TokenProvider {
    String getToken(String clientId, String clientSecret, String scopes);
}
