package com.sgm.hansimapi.infra.riot;

import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.QueueType;
import jakarta.persistence.*;
import lombok.Getter;

/** 매치 참가자별 파싱된 통계 */
@Entity
@Table(name = "match_participants",
        indexes = @Index(name = "idx_puuid_game_start", columnList = "puuid, game_start"))
@IdClass(MatchParticipantId.class)
@Getter
public class MatchParticipantEntity {

    /** Riot 매치 ID (matches.match_id 참조) */
    @Id
    @Column(name = "match_id", length = 20)
    private String matchId;

    /** 참가자 PUUID */
    @Id
    @Column(name = "puuid", length = 78)
    private String puuid;

    /** 승리 여부 */
    @Column(nullable = false)
    private boolean win;

    /** 킬 수 */
    @Column(nullable = false)
    private int kills;

    /** 데스 수 */
    @Column(nullable = false)
    private int deaths;

    /** 어시스트 수 */
    @Column(nullable = false)
    private int assists;

    /** 사용 챔피언 ID */
    @Column(name = "champion_id", nullable = false)
    private int championId;

    /** 사용 챔피언 이름 */
    @Column(name = "champion_name", nullable = false, length = 50)
    private String championName;

    /** 큐 ID — 420=솔로랭크, 440=자유랭크, 430=노말 */
    @Column(name = "queue_id", nullable = false)
    private int queueId;

    /** 게임 시작 시각 (Unix epoch ms) */
    @Column(name = "game_start", nullable = false)
    private long gameStart;

    protected MatchParticipantEntity() {}

    public MatchParticipantEntity(String matchId, String puuid, boolean win,
                                   int kills, int deaths, int assists,
                                   int championId, String championName,
                                   int queueId, long gameStart) {
        this.matchId      = matchId;
        this.puuid        = puuid;
        this.win          = win;
        this.kills        = kills;
        this.deaths       = deaths;
        this.assists      = assists;
        this.championId   = championId;
        this.championName = championName;
        this.queueId      = queueId;
        this.gameStart    = gameStart;
    }

    public Match toDomain() {
        return new Match(win, kills, deaths, assists,
                QueueType.from(queueId), championId, championName);
    }
}
