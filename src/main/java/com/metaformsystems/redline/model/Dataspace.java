package com.metaformsystems.redline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 *
 */
@Entity
@Table(name = "dataspaces")
public class Dataspace extends VersionedEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String profileId;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }
}
