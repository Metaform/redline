package com.metaformsystems.redline.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A virtual resource.
 */
@Entity
@Table(name = "virtual_participant_agents")
public class VirtualParticipantAgent extends VersionedEntity {
    public enum Type {
        CONTROL_PLANE,
        CREDENTIAL_SERVICE,
        DATA_PLANE
    }

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private DeploymentState state;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public DeploymentState getState() {
        return state;
    }

    public void setState(DeploymentState state) {
        this.state = state;
    }
}
