package com.metaformsystems.redline.controller;

import com.metaformsystems.redline.model.ServiceProvider;
import com.metaformsystems.redline.repository.ServiceProviderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 *
 */
@RestController
@RequestMapping("/api/redline")
public class RedlineController {

    private final ServiceProviderRepository serviceProviderRepository;

    public RedlineController(ServiceProviderRepository serviceProviderRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
    }

    @GetMapping("providers")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ServiceProvider>> getAllProviders() {
        return ResponseEntity.ok(serviceProviderRepository.findAll());
    }


}
