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

package com.metaformsystems.redline.dao;

import java.util.Map;

public class NewTransferRequest {
    private String counterPartyAddress;
    private String contractId;
    private Map<String, Object> dataDestination;
    private String transferType;

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

    public static final class Builder {
        private final NewTransferRequest newTransferRequest;

        private Builder() {
            newTransferRequest = new NewTransferRequest();
        }

        public static Builder aNewTransferRequest() {
            return new Builder();
        }

        public Builder counterPartyAddress(String counterPartyAddress) {
            newTransferRequest.setCounterPartyAddress(counterPartyAddress);
            return this;
        }

        public Builder contractId(String contractId) {
            newTransferRequest.setContractId(contractId);
            return this;
        }

        public Builder dataDestination(Map<String, Object> dataDestination) {
            newTransferRequest.setDataDestination(dataDestination);
            return this;
        }

        public Builder transferType(String transferType) {
            newTransferRequest.setTransferType(transferType);
            return this;
        }

        public NewTransferRequest build() {
            return newTransferRequest;
        }
    }
}
