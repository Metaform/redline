package com.metaformsystems.redline.infrastructure.client.identityhub.dto;

import java.util.List;
import java.util.Map;

public record IdentityHubParticipantContext(
        String id,
        String participantContextId,
        String did,
        String apiTokenAlias,
        List<String> roles,
        Map<String, Object> properties,
        Integer state,
        Long createdAt,
        Long lastModified
) {
}
