package com.metaformsystems.redline.dao;

/**
 *
 */
public record VPAResource(Long id, Type type, DeploymentState state) {
    public enum Type {
        CONTROL_PLANE,
        CREDENTIAL_SERVICE,
        DATA_PLANE
    }
}
