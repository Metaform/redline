/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package com.metaformsystems.redline.infrastructure.client.identityhub.dto;

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
