package com.metaformsystems.redline.client.identityhub.dto;

import java.util.List;

public record CredentialRequestDto(
        String issuerDid,
        String holderPid,
        List<CredentialDescriptor> credentials
) {
}
