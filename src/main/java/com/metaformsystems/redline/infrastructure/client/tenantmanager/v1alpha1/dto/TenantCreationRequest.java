package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.util.Map;

public record TenantCreationRequest(
        Map<String, Object> properties
) {
}
