package com.metaformsystems.redline.repository;

import com.metaformsystems.redline.model.ParticipantProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantProfile, Long> {
}