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

package com.metaformsystems.redline.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class DataPlaneConfig {
    @Value("${dataplane.url:http://dp.localhost/app/public/api/data}")
    private String dataPlanePublicUrl;

    @Value("${dataplane.internal.url:http://dp.localhost/app/internal/api/control}")
    private String dataPlaneInternalUrl;

    @Bean
    public WebClient dataPlanePublicClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(dataPlanePublicUrl)
                .build();
    }

    @Bean
    public WebClient dataPlaneInternalClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(dataPlaneInternalUrl)
                .build();
    }
}
