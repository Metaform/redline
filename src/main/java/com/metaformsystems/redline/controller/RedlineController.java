package com.metaformsystems.redline.controller;

import com.metaformsystems.redline.dao.DataspaceResource;
import com.metaformsystems.redline.dao.NewParticipantDeployment;
import com.metaformsystems.redline.dao.NewServiceProvider;
import com.metaformsystems.redline.dao.NewTenantRegistration;
import com.metaformsystems.redline.dao.ParticipantResource;
import com.metaformsystems.redline.dao.PartnerReferenceResource;
import com.metaformsystems.redline.dao.ServiceProviderResource;
import com.metaformsystems.redline.dao.TenantResource;
import com.metaformsystems.redline.service.ServiceProviderService;
import com.metaformsystems.redline.service.TenantService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("/api/ui")
public class RedlineController {
    private final ServiceProviderService serviceProviderService;
    private final TenantService tenantService;

    public RedlineController(ServiceProviderService serviceProviderService, TenantService tenantService) {
        this.tenantService = tenantService;
        this.serviceProviderService = serviceProviderService;
    }

    @GetMapping("dataspaces")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<DataspaceResource>> getDataspaces() {
        return ResponseEntity.ok(serviceProviderService.getDataspaces());
    }

    @GetMapping("service-providers")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ServiceProviderResource>> getServiceProviders() {
        return ResponseEntity.ok(serviceProviderService.getServiceProviders());
    }

    @PostMapping("service-providers")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ServiceProviderResource> createServiceProvider(@RequestBody NewServiceProvider newServiceProvider) {
        var saved = serviceProviderService.createServiceProvider(newServiceProvider);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("service-providers/{providerId}/tenants")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TenantResource> registerTenant(@PathVariable Long providerId,
                                                         @RequestBody NewTenantRegistration registration) {
        var tenant = tenantService.registerTenant(providerId, registration);
        return ResponseEntity.ok(tenant);
    }

    @PostMapping("service-providers/{providerId}/tenants/{tenantId}/participants/{participantId}/deployments")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ParticipantResource> deployParticipant(@RequestBody NewParticipantDeployment deployment) {
        var participant = tenantService.deployParticipant(deployment);
        return ResponseEntity.ok(participant);
    }

    @GetMapping("service-providers/{providerId}/tenants/{tenantId}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TenantResource> getTenant(@RequestBody Long providerId, @RequestBody Long tenantId) {
        var tenantResource = tenantService.getTenant(tenantId);
        // TODO auth check for provider access
        return ResponseEntity.ok(tenantResource);
    }

    @GetMapping("service-providers/{providerId}/tenants/{tenantId}/participants/participantId")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ParticipantResource> getParticipant(@RequestBody Long providerId,
                                                              @RequestBody Long tenantId,
                                                              @RequestBody Long participantId) {
        var participantResource = tenantService.getParticipant(participantId);
        // TODO auth check for provider access
        return ResponseEntity.ok(participantResource);
    }

    @GetMapping("service-providers/{providerId}/tenants/{tenantId}/participants/participantId/partners/{dataspaceId}")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<PartnerReferenceResource>> getPartners(@RequestBody Long providerId,
                                                                      @RequestBody Long tenantId,
                                                                      @RequestBody Long participantId,
                                                                      @RequestBody Long dataspaceId) {
        var references = tenantService.getPartnerReferences(participantId, dataspaceId);
        // TODO auth check for provider access
        return ResponseEntity.ok(references);
    }

}
