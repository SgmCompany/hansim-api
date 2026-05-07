package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.summary.PlayerSummary;
import com.sgm.hansimapi.domain.summary.QueueStat;
import com.sgm.hansimapi.domain.summary.Summary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Schema(description = "한심 summary 조회 응답")
public class SummaryResponse {

    @Schema(description = "조회 기간 (Unix epoch ms 기준)", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Period period;

    @Schema(
            description = "조회 기간 문자열 (KST 기준). 예: '2026-04-01 (수) 오전 6시 ~ 2026-04-02 (목) 오전 6시 (KST)'",
            type = "string",
            example = "2026-04-01 (수) 오전 6시 ~ 2026-04-02 (목) 오전 6시 (KST)",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String periodStr;

    @Schema(description = "플레이어별 큐 통계 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private final List<Player> players;

    private SummaryResponse(Period period, String periodStr, List<Player> players) {
        this.period = period;
        this.periodStr = periodStr;
        this.players = players;
    }

    public static SummaryResponse from(Summary summary) {
        return new SummaryResponse(
                Period.from(summary),
                summary.getPeriodStr(),
                summary.getPlayers().stream()
                        .map(Player::from)
                        .collect(Collectors.toList())
        );
    }

    @Getter
    @Schema(description = "조회 기간 (ISO-8601 instant 문자열)")
    static class Period {

        @Schema(
                description = "조회 시작 시각 (ISO-8601)",
                type = "string",
                format = "date-time",
                example = "2026-04-01T21:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final String start;

        @Schema(
                description = "조회 종료 시각 (ISO-8601)",
                type = "string",
                format = "date-time",
                example = "2026-04-02T21:00:00Z",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final String end;

        private Period(String start, String end) {
            this.start = start;
            this.end = end;
        }

        public static Period from(Summary summary) {
            return new Period(
                    summary.getStart().toString(),
                    summary.getEnd().toString()
            );
        }
    }

    @Getter
    @Schema(description = "플레이어별 큐 통계")
    static class Player {

        @Schema(
                description = "플레이어 Riot ID (gameName#tagLine 형식)",
                type = "string",
                example = "페이커#KR1",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final String name;

        @Schema(description = "일반 게임 통계. 해당 기간 내 게임이 없으면 null", nullable = true)
        private final Queue normal;

        @Schema(description = "솔로 랭크 통계. 해당 기간 내 게임이 없으면 null", nullable = true)
        private final Queue solo;

        @Schema(description = "자유 랭크 통계. 해당 기간 내 게임이 없으면 null", nullable = true)
        private final Queue flex;

        @Schema(description = "칼바람 나락 통계. 해당 기간 내 게임이 없으면 null", nullable = true)
        private final Queue aram;

        @Schema(description = "전체 큐 합산 총 플레이 시간 (초)", example = "10800",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int totalPlaySeconds;

        @Schema(description = "한심지수 (NINE_TO_SIX 기준)", requiredMode = Schema.RequiredMode.REQUIRED)
        private final HlsResponse hls;

        @Schema(description = "호출자 급여 기준 경제적 손실 추산", requiredMode = Schema.RequiredMode.REQUIRED)
        private final EconomicImpactResponse economicImpact;

        private Player(String name, Queue normal, Queue solo, Queue flex, Queue aram,
                       int totalPlaySeconds, HlsResponse hls, EconomicImpactResponse economicImpact) {
            this.name             = name;
            this.normal           = normal;
            this.solo             = solo;
            this.flex             = flex;
            this.aram             = aram;
            this.totalPlaySeconds = totalPlaySeconds;
            this.hls              = hls;
            this.economicImpact   = economicImpact;
        }

        public static Player from(PlayerSummary player) {
            return new Player(
                    player.getName(),
                    Queue.from(player.getNormal()),
                    Queue.from(player.getSolo()),
                    Queue.from(player.getFlex()),
                    Queue.from(player.getAram()),
                    player.getTotalPlaySeconds(),
                    HlsResponse.from(player.getHls()),
                    EconomicImpactResponse.from(player.getEconomicImpact())
            );
        }
    }

    @Getter
    @Schema(description = "큐 단위 통계")
    static class Queue {

        @Schema(
                description = "총 게임 수",
                type = "integer",
                minimum = "0",
                example = "10",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final int games;

        @Schema(
                description = "승리 수",
                type = "integer",
                minimum = "0",
                example = "6",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final int win;

        @Schema(
                description = "패배 수",
                type = "integer",
                minimum = "0",
                example = "4",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final int lose;

        @Schema(
                description = "평균 KDA (소수점 1자리)",
                type = "string",
                pattern = "^\\d+\\.\\d$",
                example = "3.5",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        private final String kda;

        @Schema(description = "큐 총 플레이 시간 (초)", example = "7200",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int totalPlaySeconds;

        @Schema(description = "판당 평균 게임 시간 (초)", example = "1800",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int avgGameDurationSeconds;

        @Schema(description = "분당 평균 CS (소수점 1자리)", example = "6.5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String avgCsPerMin;

        @Schema(description = "게임당 평균 챔피언 피해량", example = "18500",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int avgDamage;

        @Schema(description = "게임당 평균 비전 점수 (소수점 1자리)", example = "21.3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String avgVisionScore;

        @Schema(description = "멀티킬 합산 횟수", requiredMode = Schema.RequiredMode.REQUIRED)
        private final MultiKills multiKills;

        private Queue(int games, int win, int lose, String kda,
                      int totalPlaySeconds, int avgGameDurationSeconds,
                      String avgCsPerMin, int avgDamage, String avgVisionScore, MultiKills multiKills) {
            this.games = games;
            this.win = win;
            this.lose = lose;
            this.kda = kda;
            this.totalPlaySeconds = totalPlaySeconds;
            this.avgGameDurationSeconds = avgGameDurationSeconds;
            this.avgCsPerMin = avgCsPerMin;
            this.avgDamage = avgDamage;
            this.avgVisionScore = avgVisionScore;
            this.multiKills = multiKills;
        }

        static Queue from(QueueStat stat) {
            if (stat == null) return null;

            return new Queue(
                    stat.getGames(),
                    stat.getWin(),
                    stat.getLose(),
                    String.format("%.1f", stat.getKda()),
                    stat.getTotalPlaySeconds(),
                    stat.getAvgGameDurationSeconds(),
                    String.format("%.1f", stat.getAvgCsPerMin()),
                    stat.getAvgDamage(),
                    String.format("%.1f", stat.getAvgVisionScore()),
                    new MultiKills(stat.getTotalDoubleKills(), stat.getTotalTripleKills(),
                            stat.getTotalQuadraKills(), stat.getTotalPentaKills())
            );
        }
    }

    @Getter
    @Schema(description = "멀티킬 합산 횟수")
    static class MultiKills {

        @Schema(description = "더블킬 횟수", example = "3")
        private final int doubles;

        @Schema(description = "트리플킬 횟수", example = "1")
        private final int triples;

        @Schema(description = "쿼드라킬 횟수", example = "0")
        private final int quadras;

        @Schema(description = "펜타킬 횟수", example = "0")
        private final int pentas;

        MultiKills(int doubles, int triples, int quadras, int pentas) {
            this.doubles = doubles;
            this.triples = triples;
            this.quadras = quadras;
            this.pentas  = pentas;
        }
    }

}
