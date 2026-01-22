package com.metaformsystems.redline.infrastructure.client.identityhub.dto;

public record VerifiableCredentialContainer(
        Object credential,
        String format,
        String rawVc
) {
}
