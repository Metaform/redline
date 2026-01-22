package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record DataspaceDeployment(
        String id,
        Long version,
        String state,
        OffsetDateTime stateTimestamp,
        String cellId,
        String externalCellId,
        Map<String, Object> properties
) {
}
