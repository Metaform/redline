package com.metaformsystems.redline.infrastructure.client.identityhub.dto;

import java.util.Set;

public record KeyPairResource(
        String id,
        String participantContextId,
        String keyId,
        String privateKeyAlias,
        String serializedPublicKey,
        Set<String> usage,
        Boolean defaultPair,
        String groupName,
        String keyContext,
        Integer state,
        Long timestamp,
        Long createdAt,
        Long useDuration,
        Long rotationDuration
) {
}
