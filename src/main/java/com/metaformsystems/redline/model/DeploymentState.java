package com.metaformsystems.redline.model;

/**
 * Tracks the state of a resource deployment.
 */
public enum DeploymentState {
    INITIAL,
    PENDING,
    ACTIVE,
    DISPOSING,
    DISPOSED,
    LOCKED,
    OFFLINE,
    ERROR
}
