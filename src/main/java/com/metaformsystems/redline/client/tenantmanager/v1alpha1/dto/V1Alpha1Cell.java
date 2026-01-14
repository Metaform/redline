package com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record V1Alpha1Cell(
        String id,
        Long version,
        String state,
        OffsetDateTime stateTimestamp,
        String externalId,
        Map<String, Object> properties) {
}
