package com.metaformsystems.redline.dao;

import java.util.List;
import java.util.Map;

/**
 *
 */
public record NewTenantRegistration(String tenantName, List<NewDataspaceInfo> dataspaceInfos,
                                    Map<String, Object> properties) {

    public NewTenantRegistration(String tenantName, List<NewDataspaceInfo> dataspaceInfos) {
        this(tenantName, dataspaceInfos, Map.of());
    }

}
