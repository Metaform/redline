package com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto;

import java.util.List;
import java.util.Map;

public record V1Alpha1TenantPropertiesDiff(
    Map<String, Object> properties,
    List<String> removed
) {}
