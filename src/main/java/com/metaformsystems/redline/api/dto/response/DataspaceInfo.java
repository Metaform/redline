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

package com.metaformsystems.redline.api.dto.response;

import java.util.List;

/**
 *
 */
public final class DataspaceInfo extends com.metaformsystems.redline.api.dto.request.DataspaceInfo {
    private Long id;

    public DataspaceInfo(Long id, Long dataspaceId, List<String> agreementTypes, List<String> roles) {
        this.id = id;
        this.dataspaceId = dataspaceId;
        this.agreementTypes = agreementTypes;
        this.roles = roles;
    }

    public DataspaceInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
