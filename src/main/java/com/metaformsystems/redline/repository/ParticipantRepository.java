package com.metaformsystems.redline.repository;

import com.metaformsystems.redline.model.ParticipantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantProfile, Long> {
    Optional<ParticipantProfile> findByCorrelationId(String correlationId);

    Optional<ParticipantProfile> findByParticipantContextId(String participantContextId);
}