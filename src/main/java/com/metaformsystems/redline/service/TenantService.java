package com.metaformsystems.redline.service;

import com.metaformsystems.redline.client.tenantmanager.v1alpha1.TenantManagerClient;
import com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto.V1Alpha1NewTenant;
import com.metaformsystems.redline.client.tenantmanager.v1alpha1.dto.V1Alpha1ParticipantProfile;
import com.metaformsystems.redline.dao.NewParticipantDeployment;
import com.metaformsystems.redline.dao.NewTenantRegistration;
import com.metaformsystems.redline.dao.ParticipantProfileResource;
import com.metaformsystems.redline.dao.TenantResource;
import com.metaformsystems.redline.dao.VPAResource;
import com.metaformsystems.redline.model.Dataspace;
import com.metaformsystems.redline.model.DeploymentState;
import com.metaformsystems.redline.model.ParticipantProfile;
import com.metaformsystems.redline.model.Tenant;
import com.metaformsystems.redline.model.VersionedEntity;
import com.metaformsystems.redline.model.VirtualParticipantAgent;
import com.metaformsystems.redline.repository.DataspaceRepository;
import com.metaformsystems.redline.repository.ParticipantRepository;
import com.metaformsystems.redline.repository.ServiceProviderRepository;
import com.metaformsystems.redline.repository.TenantRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class TenantService {
    private final TenantRepository tenantRepository;
    private final ParticipantRepository participantRepository;
    private final DataspaceRepository dataspaceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final TenantManagerClient tenantManagerClient;

    public TenantService(TenantRepository tenantRepository,
                         ParticipantRepository participantRepository,
                         DataspaceRepository dataspaceRepository,
                         ServiceProviderRepository serviceProviderRepository,
                         TenantManagerClient tenantManagerClient) {
        this.tenantRepository = tenantRepository;
        this.participantRepository = participantRepository;
        this.dataspaceRepository = dataspaceRepository;
        this.serviceProviderRepository = serviceProviderRepository;
        this.tenantManagerClient = tenantManagerClient;
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
        participant.setTenant(tenant);

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
            var tmTenant = tenantManagerClient.createTenant(new V1Alpha1NewTenant(Map.of("name", tenant.getName())));
            tenant.setCorrelationId(tmTenant.id());
        }

        // invoke CFM and update Participant entity with correlation id, identifier, and VPAs

        var tmProfile = tenantManagerClient.createParticipantProfile(tenant.getCorrelationId(), new V1Alpha1ParticipantProfile(
                UUID.randomUUID().toString(), 0L, deployment.webDid(), tenant.getCorrelationId(), false, null, Map.of(), Map.of(), Collections.emptyList()
        ));
        participant.setCorrelationId(tmProfile.id());
        participant.setIdentifier(tmProfile.identifier());
        participant.setAgents(tmProfile.vpas().stream().map(apiVpa -> new VirtualParticipantAgent(VirtualParticipantAgent.VpaType.fromCfmName(apiVpa.type()), DeploymentState.valueOf(apiVpa.state().toUpperCase()))).collect(Collectors.toSet()));

        var saved = participantRepository.save(participant);
        return toParticipantResource(saved);
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
                com.metaformsystems.redline.dao.DeploymentState.valueOf(vpa.getState().name()))).toList();
        var dataspaces = saved.getDataspaces().stream().map(VersionedEntity::getId).toList();
        return new ParticipantProfileResource(saved.getId(), saved.getIdentifier(), vpas, dataspaces);
    }

}
