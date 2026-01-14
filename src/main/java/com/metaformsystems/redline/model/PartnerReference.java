package com.metaformsystems.redline.model;

import jakarta.persistence.Embeddable;

/**
 *
 */
@Embeddable
public record PartnerReference(String identifier, String nickname) {
}
