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

package com.metaformsystems.redline.infrastructure.client.identityhub;

import com.metaformsystems.redline.application.service.TokenProvider;
import com.metaformsystems.redline.domain.entity.ClientCredentials;
import com.metaformsystems.redline.domain.entity.Participant;
import com.metaformsystems.redline.domain.exception.ObjectNotFoundException;
import com.metaformsystems.redline.domain.repository.ParticipantRepository;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.CredentialDescriptor;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.CredentialRequestDto;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.DidRequestPayload;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.KeyDescriptor;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("IdentityHubClientImpl Tests")
class IdentityHubClientImplTest {

    private static final String ADMIN_CLIENT_ID = "admin";
    private static final String ADMIN_CLIENT_SECRET = "secret";
    private static final String TEST_TOKEN = "test-token";
    private static final String PARTICIPANT_CONTEXT_ID = "participant-123";
    private static final String CLIENT_ID = "client-123";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String HOLDER_PID = "holder-pid";
    private static final String KEY_PAIR_ID = "keypair-123";
    private final TokenProvider tokenProvider = mock();
    private final ParticipantRepository participantRepository = mock();
    private MockWebServer mockWebServer;
    private IdentityHubClientImpl identityHubClient;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(InetAddress.getByName("localhost"), TestSocketUtils.findAvailableTcpPort());

        var webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        identityHubClient = new IdentityHubClientImpl(
                webClient,
                tokenProvider,
                participantRepository,
                ADMIN_CLIENT_ID,
                ADMIN_CLIENT_SECRET
        );

        when(tokenProvider.getToken(ADMIN_CLIENT_ID, ADMIN_CLIENT_SECRET, "identity-api:read"))
                .thenReturn(TEST_TOKEN);
        when(tokenProvider.getToken(CLIENT_ID, CLIENT_SECRET, "identity-api:write identity-api:read"))
                .thenReturn(TEST_TOKEN);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @Test
    @DisplayName("should get all participants successfully")
    void getAllParticipants_success() {

        var responseBody = """
                [
                    {
                        "id": "p1",
                        "participantContextId": "context-1",
                        "did": "did:example:123",
                        "apiTokenAlias": "token-alias",
                        "roles": ["role1"],
                        "properties": {},
                        "state": 1,
                        "createdAt": 1234567890,
                        "lastModified": 1234567890
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));


        var result = identityHubClient.getAllParticipants();


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("context-1", result.getFirst().participantContextId());
    }

    @Test
    @DisplayName("should return empty list when no participants exist")
    void getAllParticipants_empty() {

        var responseBody = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));


        var result = identityHubClient.getAllParticipants();

        assertNotNull(result);
        assertEquals(0, result.size());
    }


    @Test
    @DisplayName("should get participant by context id successfully")
    void getParticipant_success() {

        var responseBody = """
                {
                    "id": "p1",
                    "participantContextId": "participant-123",
                    "did": "did:example:123",
                    "apiTokenAlias": "token-alias",
                    "roles": ["role1"],
                    "properties": {},
                    "state": 1,
                    "createdAt": 1234567890,
                    "lastModified": 1234567890
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.getParticipant(PARTICIPANT_CONTEXT_ID);


        assertNotNull(result);
        assertEquals("participant-123", result.participantContextId());
    }

    @Test
    @DisplayName("should throw exception when participant not found")
    void getParticipant_notFound() {

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> {
            identityHubClient.getParticipant(PARTICIPANT_CONTEXT_ID);
        });
    }

    @Test
    @DisplayName("should get all credentials successfully")
    void getAllCredentials_success() {

        var responseBody = """
                [
                    {
                        "id": "cred-1",
                        "credential": "eyJhbGciOiJFZERTQSJ9..."
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));


        var result = identityHubClient.getAllCredentials();


        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty list when no credentials exist")
    void getAllCredentials_empty() {

        var responseBody = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));


        var result = identityHubClient.getAllCredentials();


        assertNotNull(result);
        assertEquals(0, result.size());
    }


    @Test
    @DisplayName("should query credentials by type successfully")
    void queryCredentialsByType_success() {

        var responseBody = """
                [
                    {
                        "id": "cred-1",
                        "credential": "eyJhbGciOiJFZERTQSJ9..."
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.queryCredentialsByType(PARTICIPANT_CONTEXT_ID, "VerifiableCredential");


        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should query credentials without type filter")
    void queryCredentialsByType_withoutType() {

        var responseBody = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.queryCredentialsByType(PARTICIPANT_CONTEXT_ID, null);


        assertNotNull(result);
        assertEquals(0, result.size());
    }


    @Test
    @DisplayName("should get credential request successfully")
    void getCredentialRequest_success() {

        var responseBody = """
                {
                    "id": "cred-req-1",
                    "credential": "eyJhbGciOiJFZERTQSJ9..."
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.getCredentialRequest(PARTICIPANT_CONTEXT_ID, HOLDER_PID);


        assertNotNull(result);
    }


    @Test
    @DisplayName("should request credential successfully")
    void requestCredential_success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var request = createCredentialRequest();


        identityHubClient.requestCredential(PARTICIPANT_CONTEXT_ID, request);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
    }


    @Test
    @DisplayName("should get all key pairs successfully")
    void getAllKeyPairs_success() {

        var responseBody = """
                [
                    {
                        "keyId": "key-1",
                        "publicKeyPem": "-----BEGIN PUBLIC KEY-----..."
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));


        var result = identityHubClient.getAllKeyPairs();


        assertNotNull(result);
        assertEquals(1, result.size());
    }


    @Test
    @DisplayName("should query key pairs by participant context id successfully")
    void queryKeyPairByParticipantContextId_success() {

        var responseBody = """
                [
                    {
                        "keyId": "key-1",
                        "publicKeyPem": "-----BEGIN PUBLIC KEY-----..."
                    }
                ]
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.queryKeyPairByParticipantContextId(PARTICIPANT_CONTEXT_ID);


        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("should return empty list when no key pairs exist")
    void queryKeyPairByParticipantContextId_empty() {

        var responseBody = "[]";

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.queryKeyPairByParticipantContextId(PARTICIPANT_CONTEXT_ID);


        assertNotNull(result);
        assertEquals(0, result.size());
    }


    @Test
    @DisplayName("should get key pair by id successfully")
    void getKeyPair_success() {

        var responseBody = """
                {
                    "keyId": "key-1",
                    "publicKeyPem": "-----BEGIN PUBLIC KEY-----..."
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(responseBody));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));


        var result = identityHubClient.getKeyPair(PARTICIPANT_CONTEXT_ID, KEY_PAIR_ID);


        assertNotNull(result);
    }


    @Test
    @DisplayName("should add key pair successfully")
    void addKeyPair_success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var keyDescriptor = createKeyDescriptor();


        identityHubClient.addKeyPair(PARTICIPANT_CONTEXT_ID, keyDescriptor, true);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("PUT", recordedRequest.getMethod());
    }

    @Test
    @DisplayName("should add key pair without making default")
    void addKeyPair_withoutMakingDefault() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var keyDescriptor = createKeyDescriptor();


        identityHubClient.addKeyPair(PARTICIPANT_CONTEXT_ID, keyDescriptor, null);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("PUT", recordedRequest.getMethod());
    }


    @Test
    @DisplayName("should rotate key pair successfully")
    void rotateKeyPair_success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var keyDescriptor = createKeyDescriptor();
        Long duration = 3600L;


        identityHubClient.rotateKeyPair(PARTICIPANT_CONTEXT_ID, KEY_PAIR_ID, keyDescriptor, duration);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
    }

    @Test
    @DisplayName("should rotate key pair without duration")
    void rotateKeyPair_withoutDuration() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var keyDescriptor = createKeyDescriptor();


        identityHubClient.rotateKeyPair(PARTICIPANT_CONTEXT_ID, KEY_PAIR_ID, keyDescriptor, null);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
    }


    @Test
    @DisplayName("should revoke key pair successfully")
    void revokeKeyPair_success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var keyDescriptor = createKeyDescriptor();


        identityHubClient.revokeKeyPair(PARTICIPANT_CONTEXT_ID, KEY_PAIR_ID, keyDescriptor);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
    }


    @Test
    @DisplayName("should get DID state successfully")
    void getDidState_success() throws InterruptedException {

        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(204));

        var credentials = new ClientCredentials(CLIENT_ID, CLIENT_SECRET);
        var participant = createMockParticipant(credentials);

        when(participantRepository.findByParticipantContextId(PARTICIPANT_CONTEXT_ID))
                .thenReturn(Optional.of(participant));

        var payload = new DidRequestPayload("did:example:123");


        identityHubClient.getDidState(PARTICIPANT_CONTEXT_ID, payload);


        var recordedRequest = mockWebServer.takeRequest();
        assertNotNull(recordedRequest);
        assertEquals("POST", recordedRequest.getMethod());
    }

    // Helper methods to create DTO instances
    private CredentialRequestDto createCredentialRequest() {
        var credentials = List.of(
                new CredentialDescriptor("cred-1", "VerifiableCredential", "json-ld")
        );
        return new CredentialRequestDto("did:example:issuer", HOLDER_PID, credentials);
    }

    private KeyDescriptor createKeyDescriptor() {
        Map<String, Object> publicKeyJwk = new HashMap<>();
        publicKeyJwk.put("kty", "OKP");
        publicKeyJwk.put("crv", "Ed25519");

        Map<String, Object> keyGeneratorParams = new HashMap<>();
        keyGeneratorParams.put("algorithm", "Ed25519");

        Set<String> usage = new HashSet<>();
        usage.add("signing");

        return new KeyDescriptor(
                "resource-1",
                "key-1",
                "privateKeyAlias",
                "-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE+tK/...\n-----END PUBLIC KEY-----",
                publicKeyJwk,
                keyGeneratorParams,
                "OKP",
                usage,
                true
        );
    }

    // Helper method to create mock participant
    private Participant createMockParticipant(ClientCredentials credentials) {
        var participant = new Participant();
        participant.setClientCredentials(credentials);
        participant.setParticipantContextId(PARTICIPANT_CONTEXT_ID);
        return participant;
    }
}
