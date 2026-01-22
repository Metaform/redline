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

import java.time.OffsetDateTime;
import java.util.Map;

public record VerifiableCredentialResource(
        String id,
        String participantContextId,
        String holderId,
        String issuerId,
        VerifiableCredentialContainer verifiableCredential,
        String usage,
        Integer state,
        Long timestamp,
        Long createdAt,
        OffsetDateTime timeOfLastStatusUpdate,
        Map<String, Object> metadata
) {
}
