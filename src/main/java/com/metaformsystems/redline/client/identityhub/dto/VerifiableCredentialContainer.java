package com.metaformsystems.redline.client.identityhub.dto;

public record VerifiableCredentialContainer(
        Object credential,
        String format,
        String rawVc
) {
}
