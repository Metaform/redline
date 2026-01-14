package com.metaformsystems.redline.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents an identity participating in one or more dataspaces.
 */
@Entity
@Table(name = "participants")
public class ParticipantProfile extends VersionedEntity {

    private String identifier;
    private String correlationId;

    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @ManyToMany
    @JoinTable(
            name = "participant_dataspace",
            joinColumns = @JoinColumn(name = "participant_id"),
            inverseJoinColumns = @JoinColumn(name = "dataspace_id")
    )
    private Set<Dataspace> dataspaces = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "participant_id")
    private Set<VirtualParticipantAgent> agents = new HashSet<>();
    private String participantContextId;
    @Embedded
    private ClientCredentials clientCredentials;

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

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Set<VirtualParticipantAgent> getAgents() {
        return agents;
    }

    public void setAgents(Set<VirtualParticipantAgent> agents) {
        this.agents = agents;
    }

    public String getParticipantContextId() {
        return participantContextId;
    }

    public void setParticipantContextId(String participantContextId) {
        this.participantContextId = participantContextId;
    }

    public ClientCredentials getClientCredentials() {
        return clientCredentials;
    }

    public void setClientCredentials(ClientCredentials clientCredentials) {
        this.clientCredentials = clientCredentials;
    }
}
