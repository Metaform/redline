package com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto;

import java.util.List;

public record V1Alpha1DataspaceSpec(
    List<V1Alpha1CredentialSpec> credentialSpecs,
    List<String> protocolStack
) {}
