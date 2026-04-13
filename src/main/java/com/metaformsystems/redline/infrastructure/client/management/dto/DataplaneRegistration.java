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

import java.util.List;

public class DataplaneRegistration {
    private String id;
    private List<String> allowedTransferTypes;
    private String url;
    private List<String> labels;


    public List<String> getAllowedTransferTypes() {
        return allowedTransferTypes;
    }

    public void setAllowedTransferTypes(List<String> allowedTransferTypes) {
        this.allowedTransferTypes = allowedTransferTypes;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getLabels() {
        return labels;
    }

    public static final class Builder {
        private final DataplaneRegistration dataplaneRegistration;

        private Builder() {
            dataplaneRegistration = new DataplaneRegistration();
        }

        public static Builder aDataplaneRegistration() {
            return new Builder();
        }

        public Builder id(String id) {
            dataplaneRegistration.id = id;
            return this;
        }

        public Builder labels(List<String> labels) {
            dataplaneRegistration.labels = labels;
            return this;
        }

        public Builder allowedTransferTypes(List<String> allowedTransferTypes) {
            dataplaneRegistration.setAllowedTransferTypes(allowedTransferTypes);
            return this;
        }

        public Builder url(String url) {
            dataplaneRegistration.setUrl(url);
            return this;
        }

        public DataplaneRegistration build() {
            return dataplaneRegistration;
        }

    }
}
