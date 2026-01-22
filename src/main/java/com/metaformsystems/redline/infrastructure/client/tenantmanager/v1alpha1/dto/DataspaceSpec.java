package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.util.List;

public record DataspaceSpec(
        List<CredentialSpec> credentialSpecs,
        List<String> protocolStack
) {
}
