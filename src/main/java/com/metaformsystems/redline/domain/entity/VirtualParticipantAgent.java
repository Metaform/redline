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

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A virtual resource associated with a {@link Participant}.
 */
@Entity
@Table(name = "virtual_participant_agents")
public class VirtualParticipantAgent extends VersionedEntity {
    @Enumerated(EnumType.STRING)
    private VpaType type;
    @Enumerated(EnumType.STRING)
    private DeploymentState state;

    public VirtualParticipantAgent(VpaType vpaType, DeploymentState state) {
        this.type = vpaType;
        this.state = state;
    }

    public VirtualParticipantAgent() {

    }

    public VpaType getType() {
        return type;
    }

    public void setType(VpaType vpaType) {
        this.type = vpaType;
    }

    public DeploymentState getState() {
        return state;
    }

    public void setState(DeploymentState state) {
        this.state = state;
    }

    public enum VpaType {
        CONTROL_PLANE("cfm.connector"),
        CREDENTIAL_SERVICE("cfm.credentialservice"),
        DATA_PLANE("cfm.dataplane");

        private final String cfmName;

        VpaType(String cfmName) {
            this.cfmName = cfmName;
        }

        public static VpaType fromCfmName(String cfmName) {
            for (VpaType vpaType : VpaType.values()) {
                if (vpaType.cfmName.equals(cfmName)) {
                    return vpaType;
                }
            }
            return null;
        }

        public String getCfmName() {
            return cfmName;
        }
    }
}
