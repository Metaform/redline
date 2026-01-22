package com.metaformsystems.redline.infrastructure.client.identityhub.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record VerifiableCredentialResource(
        String id,
        String participantContextId,
        String holderId,
        String issuerId,
        VerifiableCredentialContainer verifiableCredential,
        String usage,
        Integer state,
        Long timestamp,
        Long createdAt,
        OffsetDateTime timeOfLastStatusUpdate,
        Map<String, Object> metadata
) {
}
