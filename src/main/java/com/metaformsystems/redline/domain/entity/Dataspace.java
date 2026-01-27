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

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a dataspace ecosystem. A dataspace has one or more {@link DataspaceProfile}s. It may have multiple profiles
 * if more than one protocol version or policy set is supported.
 */
@Entity
@Table(name = "dataspaces")
public class Dataspace extends VersionedEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @OneToMany
    @JoinColumn(name = "dataspace_id")
    private Set<DataspaceProfile> profiles = new HashSet<>();

    @Column(name = "properties", columnDefinition = "TEXT")
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> properties = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<DataspaceProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<DataspaceProfile> profiles) {
        this.profiles = profiles;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
