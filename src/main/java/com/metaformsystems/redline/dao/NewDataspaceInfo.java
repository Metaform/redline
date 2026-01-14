package com.metaformsystems.redline.dao;

import java.util.List;

/**
 *
 */
public class NewDataspaceInfo {
    protected Long dataspaceId;
    protected List<String> agreementTypes;
    protected List<String> roles;

    public NewDataspaceInfo(Long dataspaceId, List<String> agreementTypes, List<String> roles) {
        this.dataspaceId = dataspaceId;
        this.agreementTypes = agreementTypes;
        this.roles = roles;
    }

    public NewDataspaceInfo() {
    }

    public Long getDataspaceId() {
        return dataspaceId;
    }

    public void setDataspaceId(Long dataspaceId) {
        this.dataspaceId = dataspaceId;
    }

    public List<String> getAgreementTypes() {
        return agreementTypes;
    }

    public void setAgreementTypes(List<String> agreementTypes) {
        this.agreementTypes = agreementTypes;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
