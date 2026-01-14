package com.metaformsystems.redline.dao;

import java.util.List;

/**
 *
 */
public final class DataspaceInfo extends NewDataspaceInfo {
    private Long id;

    public DataspaceInfo(Long id, Long dataspaceId, List<String> agreementTypes, List<String> roles) {
        this.id = id;
        this.dataspaceId = dataspaceId;
        this.agreementTypes = agreementTypes;
        this.roles = roles;
    }

    public DataspaceInfo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
