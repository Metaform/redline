package com.metaformsystems.redline.dao;

import java.util.List;

/**
 *
 */
public record ParticipantProfileResource(Long id,
                                         String identifier,
                                         List<VPAResource> agents,
                                         List<DataspaceInfo> infos) {

}
