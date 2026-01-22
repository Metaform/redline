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

import com.metaformsystems.redline.infrastructure.client.identityhub.dto.CredentialRequestDto;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.DidRequestPayload;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.IdentityHubParticipantContext;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.KeyDescriptor;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.KeyPairResource;
import com.metaformsystems.redline.infrastructure.client.identityhub.dto.VerifiableCredentialResource;

import java.util.List;

/**
 * HTTP client for the IdentityHub Identity API
 */
public interface IdentityHubClient {

    // Participant Context operations
    List<IdentityHubParticipantContext> getAllParticipants();

    IdentityHubParticipantContext getParticipant(String participantContextId);

    // Verifiable Credentials operations
    List<VerifiableCredentialResource> getAllCredentials();

    List<VerifiableCredentialResource> queryCredentialsByType(String participantContextId, String type);

    VerifiableCredentialResource getCredentialRequest(String participantContextId, String holderPid);

    void requestCredential(String participantContextId, CredentialRequestDto request);

    // Key Pairs operations
    List<KeyPairResource> getAllKeyPairs();

    List<KeyPairResource> queryKeyPairByParticipantContextId(String participantContextId);

    KeyPairResource getKeyPair(String participantContextId, String keyPairId);

    void addKeyPair(String participantContextId, KeyDescriptor keyDescriptor, Boolean makeDefault);

    void rotateKeyPair(String participantContextId, String keyPairId, KeyDescriptor keyDescriptor, Long duration);

    void revokeKeyPair(String participantContextId, String keyPairId, KeyDescriptor keyDescriptor);

    // DID operations
    void getDidState(String participantContextId, DidRequestPayload payload);
}
