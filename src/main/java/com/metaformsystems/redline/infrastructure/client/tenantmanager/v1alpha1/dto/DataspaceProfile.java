package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.util.List;
import java.util.Map;

public record DataspaceProfile(
        String id,
        Long version,
        DataspaceSpec dataspaceSpec,
        List<String> artifacts,
        List<DataspaceDeployment> deployments,
        Map<String, Object> properties
) {
}
