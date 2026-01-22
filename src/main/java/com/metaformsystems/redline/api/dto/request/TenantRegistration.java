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

package com.metaformsystems.redline.api.dto.request;

import java.util.List;
import java.util.Map;

/**
 *
 */
public record TenantRegistration(String tenantName, List<DataspaceInfo> dataspaceInfos,
                                 Map<String, Object> properties) {

    public TenantRegistration(String tenantName, List<DataspaceInfo> dataspaceInfos) {
        this(tenantName, dataspaceInfos, Map.of());
    }

}
