package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.util.Map;

public record V1Alpha1Tenant(
        String id,
        Long version,
        Map<String, Object> properties
) {
}
