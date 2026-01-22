package com.metaformsystems.redline.dao;

import java.util.List;
import java.util.Map;

/**
 *
 */
public record TenantResource(Long id, Long providerId, String name, List<ParticipantResource> participants,
                             Map<String, Object> properties) {
}
