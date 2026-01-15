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

package com.metaformsystems.redline.client.dataplane;

import com.metaformsystems.redline.client.TokenProvider;
import com.metaformsystems.redline.client.dataplane.dto.UploadResponse;
import com.metaformsystems.redline.client.management.dto.QuerySpec;
import com.metaformsystems.redline.repository.ParticipantRepository;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

@Component
public class DataPlaneApiClientImpl implements DataPlaneApiClient {
    private final WebClient dataPlaneWebClient;
    private final ParticipantRepository participantRepository;
    private final TokenProvider tokenProvider;

    public DataPlaneApiClientImpl(WebClient dataPlaneWebClient, ParticipantRepository participantRepository, TokenProvider tokenProvider) {
        this.dataPlaneWebClient = dataPlaneWebClient;
        this.participantRepository = participantRepository;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public UploadResponse uploadMultipart(String participantContextId, Map<String, String> metadata, InputStream data) {
        var bodyBuilder = new MultipartBodyBuilder();

        // Add metadata fields
        if (metadata != null) {
            metadata.forEach(bodyBuilder::part);
        }

        // Add file data
        bodyBuilder.part("file", new InputStreamResource(data))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return dataPlaneWebClient.post()
                .uri("/app/internal/api/control/upload")
                .header("Authorization", "Bearer " + getToken(participantContextId))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(bodyBuilder.build())
                .retrieve()
                .bodyToMono(UploadResponse.class)
                .block();
    }

    @Override
    public List<UploadResponse> getAllUploads(String participantContextId) {
        return dataPlaneWebClient.post()
                .uri("/app/internal/api/control/certs/request")
                .header("Authorization", "Bearer " + getToken(participantContextId))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UploadResponse>>() {
                })
                .block();
    }

    @Override
    public List<UploadResponse> queryProviderFiles(String participantContextId, QuerySpec querySpec) {
        return dataPlaneWebClient.post()
                .uri("/app/internal/api/control/certs/request")
                .bodyValue(querySpec)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<UploadResponse>>() {
                })
                .block();
    }

    @Override
    public OutputStream downloadFile(String participantContextId, String fileId) {
        byte[] fileData = dataPlaneWebClient.get()
                .uri("/app/public/api/data/certs/" + fileId)
                .header("Authorization", "Bearer " + getToken(participantContextId))
                .retrieve()
                .bodyToMono(byte[].class)
                .block();

        var outputStream = new ByteArrayOutputStream();
        try {
            if (fileData != null) {
                outputStream.write(fileData);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to write file data to output stream", e);
        }
        return outputStream;
    }

    private String getToken(String participantContextId) {
        var participantProfile = participantRepository.findByParticipantContextId(participantContextId)
                .orElseThrow(() -> new IllegalArgumentException("Participant not found with context id: " + participantContextId));

        return tokenProvider.getToken(participantProfile.getClientCredentials().clientId(), participantProfile.getClientCredentials().clientSecret(), "management-api:write management-api:read");
    }
}
