package com.metaformsystems.redline.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@Entity
@Table(name = "tenants")
public class Tenant extends VersionedEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    @OneToMany
    @JoinColumn(name = "tenant_id")
    private Set<Participant> participants = new HashSet<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }
}
