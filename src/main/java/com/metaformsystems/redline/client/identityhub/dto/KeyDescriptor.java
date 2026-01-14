package com.metaformsystems.redline.client.identityhub.dto;

import java.util.Map;
import java.util.Set;

public record KeyDescriptor(
        String resourceId,
        String keyId,
        String privateKeyAlias,
        String publicKeyPem,
        Map<String, Object> publicKeyJwk,
        Map<String, Object> keyGeneratorParams,
        String type,
        Set<String> usage,
        Boolean active
) {
}
