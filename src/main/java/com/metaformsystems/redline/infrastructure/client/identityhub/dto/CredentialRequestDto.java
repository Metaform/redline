package com.metaformsystems.redline.infrastructure.client.identityhub.dto;

import java.util.List;

public record CredentialRequestDto(
        String issuerDid,
        String holderPid,
        List<CredentialDescriptor> credentials
) {
}
