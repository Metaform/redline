package com.metaformsystems.redline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Entity
@Table(name = "participants")
public class Participant extends VersionedEntity {

    @NotBlank
    @Column(nullable = false)
    private String identifier;

    @ManyToMany
    @JoinTable(
        name = "participant_dataspace",
        joinColumns = @JoinColumn(name = "participant_id"),
        inverseJoinColumns = @JoinColumn(name = "dataspace_id")
    )
    private Set<Dataspace> dataspaces = new HashSet<>();


    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Set<Dataspace> getDataspaces() {
        return dataspaces;
    }

    public void setDataspaces(Set<Dataspace> dataspaces) {
        this.dataspaces = dataspaces;
    }
}
