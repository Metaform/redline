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

package com.metaformsystems.redline.infrastructure.client.tenantmanager.v1alpha1.dto;

import java.time.OffsetDateTime;
import java.util.Map;

public record VirtualParticipantAgent(
        String id,
        Long version,
        String state,
        OffsetDateTime stateTimestamp,
        String type,
        String cellId,
        Map<String, Object> properties
) {
}
