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

package com.metaformsystems.redline.client.management.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class TransferRequest {
    @JsonProperty("@context")
    private final String[] context = new String[]{
            "https://w3id.org/edc/connector/management/v2",
    };
    @JsonProperty("@type")
    private final String type = "TransferRequest";
    private String protocol = "dataspace-protocol-http:2025-1";
    private String counterPartyAddress;
    private String contractId;
    private Map<String, Object> dataDestination;
    private String transferType;

    public String[] getContext() {
        return context;
    }

    public String getType() {
        return type;
    }

    public String getCounterPartyAddress() {
        return counterPartyAddress;
    }

    public void setCounterPartyAddress(String counterPartyAddress) {
        this.counterPartyAddress = counterPartyAddress;
    }

    public String getContractId() {
        return contractId;
    }

    public void setContractId(String contractId) {
        this.contractId = contractId;
    }

    public Map<String, Object> getDataDestination() {
        return dataDestination;
    }

    public void setDataDestination(Map<String, Object> dataDestination) {
        this.dataDestination = dataDestination;
    }

    public String getTransferType() {
        return transferType;
    }

    public void setTransferType(String transferType) {
        this.transferType = transferType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public static final class Builder {
        private final TransferRequest transferRequest;

        private Builder() {
            transferRequest = new TransferRequest();
        }

        public static Builder aTransferRequest() {
            return new Builder();
        }

        public Builder counterPartyAddress(String counterPartyAddress) {
            transferRequest.setCounterPartyAddress(counterPartyAddress);
            return this;
        }

        public Builder contractId(String contractId) {
            transferRequest.setContractId(contractId);
            return this;
        }

        public Builder dataDestination(Map<String, Object> dataDestination) {
            transferRequest.setDataDestination(dataDestination);
            return this;
        }

        public Builder transferType(String transferType) {
            transferRequest.setTransferType(transferType);
            return this;
        }

        public TransferRequest build() {
            return transferRequest;
        }
    }
}
