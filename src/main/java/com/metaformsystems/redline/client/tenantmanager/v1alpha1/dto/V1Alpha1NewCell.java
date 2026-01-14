package com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record V1Alpha1NewCell(
    String state,
    OffsetDateTime stateTimestamp,
    String externalId,
    Map<String, Object> properties
) {}
