package com.sgm.hansimapi.infra.riot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchParticipantJpaRepository extends JpaRepository<MatchParticipantEntity, MatchParticipantId> {

    List<MatchParticipantEntity> findByPuuidAndGameStartBetween(String puuid, long startMs, long endMs);
}
