package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.util.List;
import java.util.Map;

public record TenantPropertiesDiff(
        Map<String, Object> properties,
        List<String> removed
) {
}
