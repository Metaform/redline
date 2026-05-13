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

package com.metaformsystems.redline.infrastructure.client.management.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class CatalogRequest {
    @JsonProperty("@context")
    private final String[] context = new String[]{
            "https://w3id.org/edc/connector/management/v2",
    };
    @JsonProperty("@type")
    private final String type = "CatalogRequest";
    private String protocol = "http-dsp-profile-2025-1";
    private String counterPartyAddress;
    private String counterPartyId;

    public String[] getContext() {
        return context;
    }

    public String getType() {
        return type;
    }

    public String getCounterPartyAddress() {
        return counterPartyAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getCounterPartyId() {
        return counterPartyId;
    }

    public static final class Builder {
        private final CatalogRequest transferRequest;

        private Builder() {
            transferRequest = new CatalogRequest();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder counterPartyAddress(String counterPartyAddress) {
            transferRequest.counterPartyAddress = counterPartyAddress;
            return this;
        }

        public Builder protocol(String protocol) {
            transferRequest.protocol = protocol;
            return this;
        }

        public Builder counterPartyId(String counterPartyId) {
            transferRequest.counterPartyId = counterPartyId;
            return this;
        }

        public CatalogRequest build() {
            return transferRequest;
        }
    }
}
