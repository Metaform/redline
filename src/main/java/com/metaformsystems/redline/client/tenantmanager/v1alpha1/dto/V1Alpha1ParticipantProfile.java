package com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto;

import java.util.List;
import java.util.Map;

public record V1Alpha1ParticipantProfile(
    String id,
    Long version,
    String identifier,
    String tenantId,
    Boolean error,
    String errorDetail,
    Map<String, List<String>> participantRoles,
    Map<String, Object> properties,
    List<V1Alpha1VirtualParticipantAgent> vpas
) {}
