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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.TestSocketUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.InetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("OAuth2TokenProvider Tests")
class OAuth2TokenProviderTest {

    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-secret";
    private static final String SCOPES = "identity-api:read identity-api:write";

    private MockWebServer mockWebServer;
    private OAuth2TokenProvider tokenProvider;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(InetAddress.getByName("localhost"), TestSocketUtils.findAvailableTcpPort());

        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        tokenProvider = new OAuth2TokenProvider(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    @DisplayName("should get access token successfully")
    void getToken_success() throws InterruptedException {
        var responseBody = """
                {
                    "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "expires_in": 3600,
                    "refresh_expires_in": 0,
                    "token_type": "Bearer",
                    "not-before-policy": 0,
                    "scope": "identity-api:read identity-api:write"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);

        assertNotNull(result);
        assertEquals("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...", result);

        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/", recordedRequest.getPath());

        // Verify request body contains all expected parameters
        var requestBody = recordedRequest.getBody().readUtf8();
        assertNotNull(requestBody);
        assertRequestBodyContains(requestBody, "grant_type=client_credentials");
        assertRequestBodyContains(requestBody, "client_id=" + CLIENT_ID);
        assertRequestBodyContains(requestBody, "client_secret=" + CLIENT_SECRET);
        assertRequestBodyContains(requestBody, "scope=identity-api:read");
        assertRequestBodyContains(requestBody, "identity-api:write");
    }

    @Test
    @DisplayName("should handle token with different scope")
    void getToken_differentScope() throws InterruptedException {
        var customScopes = "read:only";
        var responseBody = """
                {
                    "access_token": "different-token-value",
                    "expires_in": 7200,
                    "token_type": "Bearer"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, customScopes);

        assertNotNull(result);
        assertEquals("different-token-value", result);

        var recordedRequest = mockWebServer.takeRequest();
        var requestBody = recordedRequest.getBody().readUtf8();
        assertRequestBodyContains(requestBody, "scope=" + customScopes);
    }

    @Test
    @DisplayName("should handle token response with minimal fields")
    void getToken_minimalResponse() {
        var responseBody = """
                {
                    "access_token": "minimal-token"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);

        assertNotNull(result);
        assertEquals("minimal-token", result);
    }

    @Test
    @DisplayName("should include form content type in request")
    void getToken_contentType() throws InterruptedException {
        var responseBody = """
                {
                    "access_token": "test-token"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);

        var recordedRequest = mockWebServer.takeRequest();
        var contentType = recordedRequest.getHeader("Content-Type");
        assertNotNull(contentType);
        assertEquals("application/x-www-form-urlencoded", contentType);
    }

    @Test
    @DisplayName("should handle special characters in client credentials")
    void getToken_specialCharacters() throws InterruptedException {
        var specialClientId = "client@example.com";
        var specialClientSecret = "secret&with#special$chars";
        var responseBody = """
                {
                    "access_token": "special-char-token"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(specialClientId, specialClientSecret, SCOPES);

        assertNotNull(result);
        assertEquals("special-char-token", result);

        var recordedRequest = mockWebServer.takeRequest();
        var requestBody = recordedRequest.getBody().readUtf8();
        assertNotNull(requestBody);
        // Verify that the request was properly formed even with special characters
        assertRequestBodyContains(requestBody, "grant_type=client_credentials");
    }

    @Test
    @DisplayName("should handle empty scope")
    void getToken_emptyScope() {
        var responseBody = """
                {
                    "access_token": "token-with-empty-scope"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, "");

        assertNotNull(result);
        assertEquals("token-with-empty-scope", result);
    }

    @Test
    @DisplayName("should handle long access token")
    void getToken_longAccessToken() {
        var longToken = "eyJhbGciOiJSUzI1NiIsInR5cC6IkpXVCIsImtpZCI6IjEyMzQ1Njc4OTAifQ." +
                "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ." +
                "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        var responseBody = "{\"access_token\": \"" + longToken + "\"}";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);

        assertNotNull(result);
        assertEquals(longToken, result);
    }

    @Test
    @DisplayName("should handle multiple consecutive token requests")
    void getToken_multipleRequests() {
        var token1 = "first-token";
        var token2 = "second-token";
        var token3 = "third-token";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"access_token\": \"" + token1 + "\"}"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"access_token\": \"" + token2 + "\"}"));

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"access_token\": \"" + token3 + "\"}"));

        var result1 = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);
        var result2 = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);
        var result3 = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);

        assertEquals(token1, result1);
        assertEquals(token2, result2);
        assertEquals(token3, result3);

        // Verify all three requests were made
        assertEquals(3, mockWebServer.getRequestCount());
    }

    @Test
    @DisplayName("should handle response with additional fields")
    void getToken_responseWithAdditionalFields() {
        var responseBody = """
                {
                    "access_token": "token-with-extras",
                    "expires_in": 3600,
                    "refresh_expires_in": 86400,
                    "refresh_token": "refresh-token-value",
                    "token_type": "Bearer",
                    "id_token": "id-token-value",
                    "not-before-policy": 0,
                    "session_state": "session-123",
                    "scope": "identity-api:read identity-api:write",
                    "custom_claim": "custom_value"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var result = tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES);

        assertNotNull(result);
        assertEquals("token-with-extras", result);
    }

    @Test
    @DisplayName("should throw exception on null response")
    void getToken_nullResponse() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("null"));

        assertThat(tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES)).isNull();
    }

    @Test
    @DisplayName("should handle error response gracefully")
    void getToken_errorResponse() {
        var errorResponse = """
                {
                    "error": "invalid_client",
                    "error_description": "Client authentication failed"
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json")
                .setBody(errorResponse));

        assertThrows(Exception.class, () ->
                tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, SCOPES)
        );
    }

    // Helper method to assert request body contains a substring
    private void assertRequestBodyContains(String requestBody, String expectedSubstring) {
        assertNotNull(requestBody, "Request body should not be null");
        assertTrue(requestBody.contains(expectedSubstring),
                "Request body should contain '" + expectedSubstring + "', but was: " + requestBody);
    }
}
