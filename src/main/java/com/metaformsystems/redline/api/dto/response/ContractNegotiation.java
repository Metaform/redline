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

package com.metaformsystems.redline.api.dto.response;

import com.metaformsystems.redline.infrastructure.client.management.dto.ContractOffer;

import java.util.ArrayList;
import java.util.List;

public class ContractNegotiation {
    private String id;
    private String state;
    private String correlationId;
    private String counterPartyId;
    private String counterPartyAddress;
    private String protocol;
    private String participantContextId;
    private String type;
    private String contractAgreementId;
    private List<ContractOffer> contractOffers = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getCounterPartyId() {
        return counterPartyId;
    }

    public void setCounterPartyId(String counterPartyId) {
        this.counterPartyId = counterPartyId;
    }

    public String getCounterPartyAddress() {
        return counterPartyAddress;
    }

    public void setCounterPartyAddress(String counterPartyAddress) {
        this.counterPartyAddress = counterPartyAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getParticipantContextId() {
        return participantContextId;
    }

    public void setParticipantContextId(String participantContextId) {
        this.participantContextId = participantContextId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContractAgreementId() {
        return contractAgreementId;
    }

    public void setContractAgreementId(String contractAgreementId) {
        this.contractAgreementId = contractAgreementId;
    }

    public List<ContractOffer> getContractOffers() {
        return contractOffers;
    }

    public void setContractOffers(List<ContractOffer> contractOffers) {
        this.contractOffers = contractOffers;
    }


    public static final class Builder {
        private final ContractNegotiation contractNegotiation;

        private Builder() {
            contractNegotiation = new ContractNegotiation();
        }

        public static Builder aContractNegotiationDto() {
            return new Builder();
        }

        public Builder id(String id) {
            contractNegotiation.setId(id);
            return this;
        }

        public Builder state(String state) {
            contractNegotiation.setState(state);
            return this;
        }

        public Builder correlationId(String correlationId) {
            contractNegotiation.setCorrelationId(correlationId);
            return this;
        }

        public Builder counterPartyId(String counterPartyId) {
            contractNegotiation.setCounterPartyId(counterPartyId);
            return this;
        }

        public Builder counterPartyAddress(String counterPartyAddress) {
            contractNegotiation.setCounterPartyAddress(counterPartyAddress);
            return this;
        }

        public Builder protocol(String protocol) {
            contractNegotiation.setProtocol(protocol);
            return this;
        }

        public Builder participantContextId(String participantContextId) {
            contractNegotiation.setParticipantContextId(participantContextId);
            return this;
        }

        public Builder type(String type) {
            contractNegotiation.setType(type);
            return this;
        }

        public Builder contractAgreementId(String contractAgreementId) {
            contractNegotiation.setContractAgreementId(contractAgreementId);
            return this;
        }

        public Builder contractOffers(List<ContractOffer> contractOffers) {
            contractNegotiation.setContractOffers(contractOffers);
            return this;
        }

        public ContractNegotiation build() {
            return contractNegotiation;
        }
    }
}
