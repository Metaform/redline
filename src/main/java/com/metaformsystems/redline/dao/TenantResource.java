package com.metaformsystems.redline.dao;

import java.util.List;

/**
 *
 */
public record TenantResource(Long id, Long providerId, String name, List<ParticipantResource> participants) {
}
