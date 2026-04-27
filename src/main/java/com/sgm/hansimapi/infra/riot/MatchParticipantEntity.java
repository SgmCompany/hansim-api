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

    /** 포지션 (TOP/JUNGLE/MID/BOTTOM/UTILITY) */
    @Column(name = "team_position", length = 10)
    private String teamPosition;

    /** 미니언 처치 수 */
    @Column(name = "total_minions_killed", nullable = false)
    private int totalMinionsKilled;

    /** 중립 몬스터 처치 수 */
    @Column(name = "neutral_minions_killed", nullable = false)
    private int neutralMinionsKilled;

    /** 게임 시간 (초) */
    @Column(name = "game_duration", nullable = false)
    private int gameDuration;

    /** 챔피언에게 가한 총 피해량 */
    @Column(name = "total_damage_dealt_to_champions", nullable = false)
    private int totalDamageDealtToChampions;

    /** 비전 점수 */
    @Column(name = "vision_score", nullable = false)
    private int visionScore;

    /** 설치한 와드 수 */
    @Column(name = "wards_placed", nullable = false)
    private int wardsPlaced;

    /** 제거한 와드 수 */
    @Column(name = "wards_killed", nullable = false)
    private int wardsKilled;

    /** 더블킬 횟수 */
    @Column(name = "double_kills", nullable = false)
    private int doubleKills;

    /** 트리플킬 횟수 */
    @Column(name = "triple_kills", nullable = false)
    private int tripleKills;

    /** 쿼드라킬 횟수 */
    @Column(name = "quadra_kills", nullable = false)
    private int quadraKills;

    /** 펜타킬 횟수 */
    @Column(name = "penta_kills", nullable = false)
    private int pentaKills;

    protected MatchParticipantEntity() {}

    public MatchParticipantEntity(String matchId, String puuid, boolean win,
                                   int kills, int deaths, int assists,
                                   int championId, String championName,
                                   int queueId, long gameStart,
                                   String teamPosition,
                                   int totalMinionsKilled, int neutralMinionsKilled, int gameDuration,
                                   int totalDamageDealtToChampions,
                                   int visionScore, int wardsPlaced, int wardsKilled,
                                   int doubleKills, int tripleKills, int quadraKills, int pentaKills) {
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
        this.teamPosition = teamPosition;
        this.totalMinionsKilled = totalMinionsKilled;
        this.neutralMinionsKilled = neutralMinionsKilled;
        this.gameDuration = gameDuration;
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
        this.visionScore  = visionScore;
        this.wardsPlaced  = wardsPlaced;
        this.wardsKilled  = wardsKilled;
        this.doubleKills  = doubleKills;
        this.tripleKills  = tripleKills;
        this.quadraKills  = quadraKills;
        this.pentaKills   = pentaKills;
    }

    public Match toDomain() {
        return new Match(win, kills, deaths, assists,
                QueueType.from(queueId), championId, championName,
                teamPosition,
                totalMinionsKilled, neutralMinionsKilled, gameDuration,
                totalDamageDealtToChampions,
                visionScore, wardsPlaced, wardsKilled,
                doubleKills, tripleKills, quadraKills, pentaKills,
                gameStart);
    }
}
