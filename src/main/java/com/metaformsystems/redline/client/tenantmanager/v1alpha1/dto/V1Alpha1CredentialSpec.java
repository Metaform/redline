package com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto;

public record V1Alpha1CredentialSpec(
        String id,
        String type,
        String issuer,
        String format,
        String role) {
}
