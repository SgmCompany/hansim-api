package com.sgm.hansimapi.infra.riot;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchJpaRepository extends JpaRepository<MatchEntity, String> {
}
