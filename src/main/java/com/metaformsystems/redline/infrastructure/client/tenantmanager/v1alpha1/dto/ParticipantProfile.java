package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.util.List;
import java.util.Map;

public record ParticipantProfile(
        String id,
        Long version,
        String identifier,
        String tenantId,
        Boolean error,
        String errorDetail,
        Map<String, List<String>> participantRoles,
        Map<String, Object> properties,
        List<VirtualParticipantAgent> vpas
) {
}
