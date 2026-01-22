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

/**
 *
 */
public class DataspaceInfo {
    protected Long dataspaceId;
    protected List<String> agreementTypes;
    protected List<String> roles;

    public DataspaceInfo(Long dataspaceId, List<String> agreementTypes, List<String> roles) {
        this.dataspaceId = dataspaceId;
        this.agreementTypes = agreementTypes;
        this.roles = roles;
    }

    public DataspaceInfo() {
    }

    public Long getDataspaceId() {
        return dataspaceId;
    }

    public void setDataspaceId(Long dataspaceId) {
        this.dataspaceId = dataspaceId;
    }

    public List<String> getAgreementTypes() {
        return agreementTypes;
    }

    public void setAgreementTypes(List<String> agreementTypes) {
        this.agreementTypes = agreementTypes;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
