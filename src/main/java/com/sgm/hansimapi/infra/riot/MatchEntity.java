package com.sgm.hansimapi.infra.riot;

import jakarta.persistence.*;
import jakarta.persistence.Basic;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** Riot 매치 원본 데이터 캐시 */
@Entity
@Table(name = "matches")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class MatchEntity {

    /** Riot 매치 ID (예: KR_7654321) */
    @Id
    @Column(name = "match_id", length = 20)
    private String matchId;

    /** Riot 큐 ID — 420=솔로랭크, 440=자유랭크, 430=노말 */
    @Column(name = "queue_id", nullable = false)
    private int queueId;

    /** 게임 시작 시각 (Unix epoch ms) */
    @Column(name = "game_start", nullable = false)
    private long gameStart;

    /** Riot Match API 원본 응답 전체. 명시적 접근 시에만 로드됨 */
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "raw_data", nullable = false, columnDefinition = "JSON")
    private String rawData;

    /** 최초 저장 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    protected MatchEntity() {}

    public MatchEntity(String matchId, int queueId, long gameStart, String rawData) {
        this.matchId   = matchId;
        this.queueId   = queueId;
        this.gameStart = gameStart;
        this.rawData   = rawData;
    }
}
