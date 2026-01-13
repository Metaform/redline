package com.metaformsystems.redline.repository;

import com.metaformsystems.redline.model.Dataspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataspaceRepository extends JpaRepository<Dataspace, Long> {
}