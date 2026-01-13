package com.metaformsystems.redline.service;

import com.metaformsystems.redline.dao.DeploymentState;
import com.metaformsystems.redline.dao.NewParticipantDeployment;
import com.metaformsystems.redline.dao.NewTenantRegistration;
import com.metaformsystems.redline.dao.ParticipantProfileResource;
import com.metaformsystems.redline.dao.TenantResource;
import com.metaformsystems.redline.dao.VPAResource;
import com.metaformsystems.redline.model.Dataspace;
import com.metaformsystems.redline.model.ParticipantProfile;
import com.metaformsystems.redline.model.Tenant;
import com.metaformsystems.redline.model.VersionedEntity;
import com.metaformsystems.redline.repository.DataspaceRepository;
import com.metaformsystems.redline.repository.ParticipantRepository;
import com.metaformsystems.redline.repository.ServiceProviderRepository;
import com.metaformsystems.redline.repository.TenantRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 *
 */
@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final ParticipantRepository participantRepository;
    private final DataspaceRepository dataspaceRepository;
    private final ServiceProviderRepository serviceProviderRepository;

    public TenantService(TenantRepository tenantRepository,
                         ParticipantRepository participantRepository,
                         DataspaceRepository dataspaceRepository,
                         ServiceProviderRepository serviceProviderRepository) {
        this.tenantRepository = tenantRepository;
        this.participantRepository = participantRepository;
        this.dataspaceRepository = dataspaceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @Transactional
    TenantResource getTenant(Long id) {
        return tenantRepository.findById(id)
                .map(t -> {
                    var participants = t.getParticipants().stream()
                            .map(this::toParticipantResource).toList();
                    return new TenantResource(t.getId(), t.getName(), participants);
                })
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + id));
    }

    @Transactional
    public Tenant registerTenant(Long serviceProviderId, NewTenantRegistration registration) {
        // Create tenant
        var tenant = new Tenant();
        tenant.setName(registration.tenantName());
        tenant.setServiceProvider(serviceProviderRepository.getReferenceById(serviceProviderId));

        // Create participant with dataspaces
        var participant = new ParticipantProfile();
        participant.setIdentifier(registration.tenantName());

        // Get dataspaces
        var dataspaces = new HashSet<Dataspace>();
        for (var dataspaceId : registration.dataspaces()) {
            dataspaces.add(dataspaceRepository.getReferenceById(dataspaceId));
        }
        participant.setDataspaces(dataspaces);

        var savedTenant = tenantRepository.save(tenant);

        participantRepository.save(participant);

        savedTenant.getParticipants().add(participant);

        return savedTenant;
    }

    @Transactional
    public ParticipantProfileResource deployParticipant(NewParticipantDeployment deployment) {
        var participant = participantRepository.findById(deployment.participantId())
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + deployment.participantId()));

        var tenant = participant.getTenant();
        if (tenant.getCorrelationId() == null) {
            // Create Tenant in CFM and update tenant with correlation id
        }

        // TODO invoke CFM and update Participant entity with correlation id, identifier, and VPAs
        participant.setCorrelationId("correlation-id");
        return toParticipantResource(participantRepository.save(participant));
    }

    @Transactional
    public ParticipantProfileResource getParticipant(Long id) {
        return participantRepository.findById(id)
                .map(this::toParticipantResource)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with id: " + id));

    }

    @NonNull
    private ParticipantProfileResource toParticipantResource(ParticipantProfile saved) {
        var vpas = saved.getAgents().stream().map(vpa -> new VPAResource(vpa.getId(),
                VPAResource.Type.valueOf(vpa.getType().name()),
                DeploymentState.valueOf(vpa.getState().name()))).toList();
        var dataspaces = saved.getDataspaces().stream().map(VersionedEntity::getId).toList();
        return new ParticipantProfileResource(saved.getId(), saved.getIdentifier(), vpas, dataspaces);
    }

}
