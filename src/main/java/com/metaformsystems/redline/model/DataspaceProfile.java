package com.metaformsystems.redline.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 *
 */
@Entity
@Table(name = "dataspace_profiles")
public class DataspaceProfile extends VersionedEntity {
    private String correlationId;

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }
}
