package com.metaformsystems.redline.dao;

import java.util.List;

public class DataplaneRegistration {
    private List<String> allowedSourceTypes;
    private List<String> allowedTransferTypes;
    private String url;

    public List<String> getAllowedSourceTypes() {
        return allowedSourceTypes;
    }

    public void setAllowedSourceTypes(List<String> allowedSourceTypes) {
        this.allowedSourceTypes = allowedSourceTypes;
    }

    public List<String> getAllowedTransferTypes() {
        return allowedTransferTypes;
    }

    public void setAllowedTransferTypes(List<String> allowedTransferTypes) {
        this.allowedTransferTypes = allowedTransferTypes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static final class Builder {
        private DataplaneRegistration dataplaneRegistration;

        private Builder() {
            dataplaneRegistration = new DataplaneRegistration();
        }

        public static Builder aDataplaneRegistration() {
            return new Builder();
        }

        public Builder allowedSourceTypes(List<String> allowedSourceTypes) {
            dataplaneRegistration.setAllowedSourceTypes(allowedSourceTypes);
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
