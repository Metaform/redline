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

package com.metaformsystems.redline.domain.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Metadata associated with a {@link Participant} for a specific {@link Dataspace}.
 */
@Entity
@Table(name = "dataspace_info")
public class DataspaceInfo extends VersionedEntity {
    @NotNull
    @Column(nullable = false)
    private Long dataspaceId;

    @ElementCollection
    @CollectionTable(
            name = "dataspace_info_agreement_types",
            joinColumns = @JoinColumn(name = "dataspace_info_id")
    )
    @Column(name = "agreement_type")
    private List<String> agreementTypes = new ArrayList<>();
    @ElementCollection
    @CollectionTable(
            name = "dataspace_info_roles",
            joinColumns = @JoinColumn(name = "dataspace_info_id")
    )
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "dataspace_info_partners",
            joinColumns = @JoinColumn(name = "dataspace_info_id")
    )
    private List<PartnerReference> partners = new ArrayList<>();

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> properties;


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

    public List<PartnerReference> getPartners() {
        return partners;
    }

    public void setPartners(List<PartnerReference> partners) {
        this.partners = partners;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
