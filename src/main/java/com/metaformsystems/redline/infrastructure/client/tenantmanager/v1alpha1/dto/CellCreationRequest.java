package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record CellCreationRequest(
        String state,
        OffsetDateTime stateTimestamp,
        String externalId,
        Map<String, Object> properties
) {
}
