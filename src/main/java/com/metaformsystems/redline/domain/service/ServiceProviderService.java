/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package com.metaformsystems.redline.domain.service;

import com.metaformsystems.redline.api.dto.request.DataspaceRequest;
import com.metaformsystems.redline.api.dto.request.ServiceProvider;
import com.metaformsystems.redline.api.dto.response.DataspaceResponse;
import com.metaformsystems.redline.api.dto.response.ServiceProviderResponse;
import com.metaformsystems.redline.domain.entity.Dataspace;
import com.metaformsystems.redline.domain.repository.DataspaceRepository;
import com.metaformsystems.redline.domain.repository.ServiceProviderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServiceProviderService {
    private final ServiceProviderRepository serviceProviderRepository;
    private final DataspaceRepository dataspaceRepository;

    public ServiceProviderService(ServiceProviderRepository serviceProviderRepository, DataspaceRepository dataspaceRepository) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.dataspaceRepository = dataspaceRepository;
    }

    @Transactional
    public DataspaceResponse createDataspace(DataspaceRequest dataspace) {
        var entity = new Dataspace();
        entity.setName(dataspace.name());
        entity.setProperties(dataspace.properties());
        var saved = dataspaceRepository.save(entity);
        return new DataspaceResponse(saved.getId(), saved.getName(), saved.getProperties());
    }

    @Transactional
    public ServiceProviderResponse createServiceProvider(ServiceProvider provider) {
        var serviceProvider = new com.metaformsystems.redline.domain.entity.ServiceProvider();
        serviceProvider.setName(provider.name());
        var saved = serviceProviderRepository.save(serviceProvider);
        return new ServiceProviderResponse(saved.getId(), saved.getName());
    }

    @Transactional
    public List<DataspaceResponse> getDataspaces() {
        return dataspaceRepository.findAll().stream()
                .map(dataspace -> new DataspaceResponse(dataspace.getId(), dataspace.getName(), dataspace.getProperties()))
                .toList();
    }

    @Transactional
    public List<ServiceProviderResponse> getServiceProviders() {
        return serviceProviderRepository.findAll().stream()
                .map(provider -> new ServiceProviderResponse(provider.getId(), provider.getName()))
                .toList();
    }


}

