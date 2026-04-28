package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.QueueType;
import com.sgm.hansimapi.domain.summary.BatchPlayerSummary;
import com.sgm.hansimapi.domain.summary.BatchSummary;
import com.sgm.hansimapi.domain.summary.ChampionStat;
import com.sgm.hansimapi.domain.summary.QueueStat;
import com.sgm.hansimapi.domain.summary.Streak;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "다중 소환사 한심 지수 조회 응답")
public class BatchSummaryResponse {

    @Schema(description = "조회 기간 (Unix epoch 기준)", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Period period;

    @Schema(
            description = "조회 기간 문자열 (KST). 예: '2026-04-01 (수) 오전 6시 ~ 2026-04-02 (목) 오전 6시 (KST)'",
            type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final String periodStr;

    @Schema(description = "소환사별 통계 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    private final List<Player> players;

    private BatchSummaryResponse(Period period, String periodStr, List<Player> players) {
        this.period = period;
        this.periodStr = periodStr;
        this.players = players;
    }

    public static BatchSummaryResponse from(BatchSummary summary) {
        return new BatchSummaryResponse(
                new Period(summary.getStart().toString(), summary.getEnd().toString()),
                summary.getPeriodStr(),
                summary.getPlayers().stream().map(Player::from).toList()
        );
    }

    @Getter
    @Schema(description = "조회 기간 (ISO-8601)")
    public static class Period {

        @Schema(description = "시작 시각 (ISO-8601)", type = "string", format = "date-time",
                example = "2026-04-01T21:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String start;

        @Schema(description = "종료 시각 (ISO-8601)", type = "string", format = "date-time",
                example = "2026-04-02T21:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String end;

        public Period(String start, String end) {
            this.start = start;
            this.end = end;
        }
    }

    @Getter
    @Schema(description = "소환사 통계")
    public static class Player {

        @Schema(description = "Riot ID (gameName#tagLine)", example = "페이커#KR1",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String riotId;

        @Schema(description = "프로필 아이콘 ID", example = "4567",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int profileIconId;

        @Schema(description = "소환사 레벨", example = "500",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final long summonerLevel;

        @Schema(description = "솔로 랭크 정보. 배치 미완료 시 null", nullable = true)
        private final RankInfo soloRank;

        @Schema(description = "자유 랭크 정보. 배치 미완료 시 null", nullable = true)
        private final RankInfo flexRank;

        @Schema(description = "조회 기간 내 큐별 통계", requiredMode = Schema.RequiredMode.REQUIRED)
        private final Queues queues;

        @Schema(description = "현재 연승/연패 스트릭", requiredMode = Schema.RequiredMode.REQUIRED)
        private final StreakInfo streak;

        @Schema(description = "조회 기간 내 챔피언별 통계 (게임 수 내림차순)", requiredMode = Schema.RequiredMode.REQUIRED)
        private final List<Champion> topChampions;

        @Schema(description = "전체 큐 합산 총 플레이 시간 (초)", example = "10800",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int totalPlaySeconds;

        @Schema(description = "한심지수 (NINE_TO_SIX 기준)", requiredMode = Schema.RequiredMode.REQUIRED)
        private final HlsResponse hls;

        private Player(String riotId, int profileIconId, long summonerLevel,
                       RankInfo soloRank, RankInfo flexRank, Queues queues,
                       StreakInfo streak, List<Champion> topChampions,
                       int totalPlaySeconds, HlsResponse hls) {
            this.riotId = riotId;
            this.profileIconId = profileIconId;
            this.summonerLevel = summonerLevel;
            this.soloRank = soloRank;
            this.flexRank = flexRank;
            this.queues = queues;
            this.streak = streak;
            this.topChampions = topChampions;
            this.totalPlaySeconds = totalPlaySeconds;
            this.hls = hls;
        }

        public static Player from(BatchPlayerSummary p) {
            return new Player(
                    p.getRiotId(),
                    p.getSummonerInfo().getProfileIconId(),
                    p.getSummonerInfo().getSummonerLevel(),
                    RankInfo.from(p.getLeagueEntry(QueueType.SOLO)),
                    RankInfo.from(p.getLeagueEntry(QueueType.FLEX)),
                    Queues.from(p),
                    StreakInfo.from(p.getStreak()),
                    p.getTopChampions().stream().map(Champion::from).toList(),
                    p.getTotalPlaySeconds(),
                    HlsResponse.from(p.getHls())
            );
        }
    }

    @Getter
    @Schema(description = "랭크 정보")
    public static class RankInfo {

        @Schema(description = "티어 (IRON~CHALLENGER)", example = "DIAMOND",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String tier;

        @Schema(description = "단계 (I~IV)", example = "II",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String rank;

        @Schema(description = "리그 포인트", example = "75",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int lp;

        @Schema(description = "시즌 총 승리 수", example = "120",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int wins;

        @Schema(description = "시즌 총 패배 수", example = "100",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int losses;

        @Schema(description = "시즌 승률 (%)", example = "54.5",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String winRate;

        private RankInfo(String tier, String rank, int lp, int wins, int losses, String winRate) {
            this.tier = tier;
            this.rank = rank;
            this.lp = lp;
            this.wins = wins;
            this.losses = losses;
            this.winRate = winRate;
        }

        public static RankInfo from(LeagueEntry entry) {
            if (entry == null) return null;
            return new RankInfo(
                    entry.getTier(),
                    entry.getRank(),
                    entry.getLeaguePoints(),
                    entry.getWins(),
                    entry.getLosses(),
                    String.format("%.1f", entry.getWinRate())
            );
        }
    }

    @Getter
    @Schema(description = "큐별 통계")
    public static class Queues {

        @Schema(description = "일반 게임. 해당 기간 게임 없으면 null", nullable = true)
        private final QueueInfo normal;

        @Schema(description = "솔로 랭크. 해당 기간 게임 없으면 null", nullable = true)
        private final QueueInfo solo;

        @Schema(description = "자유 랭크. 해당 기간 게임 없으면 null", nullable = true)
        private final QueueInfo flex;

        @Schema(description = "칼바람 나락. 해당 기간 게임 없으면 null", nullable = true)
        private final QueueInfo aram;

        private Queues(QueueInfo normal, QueueInfo solo, QueueInfo flex, QueueInfo aram) {
            this.normal = normal;
            this.solo = solo;
            this.flex = flex;
            this.aram = aram;
        }

        public static Queues from(BatchPlayerSummary p) {
            return new Queues(
                    QueueInfo.from(p.getNormal()),
                    QueueInfo.from(p.getSolo()),
                    QueueInfo.from(p.getFlex()),
                    QueueInfo.from(p.getAram())
            );
        }
    }

    @Getter
    @Schema(description = "단일 큐 통계")
    public static class QueueInfo {

        @Schema(description = "총 게임 수", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int games;

        @Schema(description = "승리 수", example = "6", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int win;

        @Schema(description = "패배 수", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int lose;

        @Schema(description = "승률 (%)", example = "60.0", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String winRate;

        @Schema(description = "평균 KDA (소수점 1자리)", example = "3.5",
                requiredMode = Schema.RequiredMode.REQUIRED)
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

        private QueueInfo(int games, int win, int lose, String winRate, String kda,
                          int totalPlaySeconds, int avgGameDurationSeconds,
                          String avgCsPerMin, int avgDamage, String avgVisionScore, MultiKills multiKills) {
            this.games = games;
            this.win = win;
            this.lose = lose;
            this.winRate = winRate;
            this.kda = kda;
            this.totalPlaySeconds = totalPlaySeconds;
            this.avgGameDurationSeconds = avgGameDurationSeconds;
            this.avgCsPerMin = avgCsPerMin;
            this.avgDamage = avgDamage;
            this.avgVisionScore = avgVisionScore;
            this.multiKills = multiKills;
        }

        public static QueueInfo from(QueueStat stat) {
            if (stat == null) return null;
            return new QueueInfo(
                    stat.getGames(),
                    stat.getWin(),
                    stat.getLose(),
                    String.format("%.1f", stat.getWinRate()),
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
    @Schema(description = "멀티킬 합산")
    public static class MultiKills {

        @Schema(description = "더블킬 횟수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int doubles;

        @Schema(description = "트리플킬 횟수", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int triples;

        @Schema(description = "쿼드라킬 횟수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int quadras;

        @Schema(description = "펜타킬 횟수", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int pentas;

        public MultiKills(int doubles, int triples, int quadras, int pentas) {
            this.doubles = doubles;
            this.triples = triples;
            this.quadras = quadras;
            this.pentas  = pentas;
        }
    }

    @Getter
    @Schema(description = "현재 연승/연패 스트릭")
    public static class StreakInfo {

        @Schema(description = "스트릭 유형 (WIN / LOSE / NONE)", example = "WIN",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final String type;

        @Schema(description = "연속 횟수", example = "3",
                requiredMode = Schema.RequiredMode.REQUIRED)
        private final int count;

        private StreakInfo(String type, int count) {
            this.type = type;
            this.count = count;
        }

        public static StreakInfo from(Streak streak) {
            return new StreakInfo(streak.getType().name(), streak.getCount());
        }
    }

    @Getter
    @Schema(description = "챔피언별 통계")
    public static class Champion {

        @Schema(description = "챔피언 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int championId;

        @Schema(description = "챔피언 이름", example = "Ahri", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String championName;

        @Schema(description = "게임 수", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int games;

        @Schema(description = "승리 수", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        private final int wins;

        @Schema(description = "승률 (%)", example = "60.0", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String winRate;

        @Schema(description = "평균 KDA", example = "4.2", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String kda;

        @Schema(description = "가장 많이 플레이한 포지션 (TOP/JUNGLE/MID/BOTTOM/UTILITY). 포지션 정보 없으면 null",
                nullable = true)
        private final String topPosition;

        private Champion(int championId, String championName, int games, int wins,
                         String winRate, String kda, String topPosition) {
            this.championId   = championId;
            this.championName = championName;
            this.games        = games;
            this.wins         = wins;
            this.winRate      = winRate;
            this.kda          = kda;
            this.topPosition  = topPosition;
        }

        public static Champion from(ChampionStat stat) {
            double wr = stat.getGames() == 0 ? 0.0 : (double) stat.getWins() / stat.getGames() * 100.0;
            return new Champion(
                    stat.getChampionId(),
                    stat.getChampionName(),
                    stat.getGames(),
                    stat.getWins(),
                    String.format("%.1f", wr),
                    String.format("%.1f", stat.getKda()),
                    stat.getTopPosition()
            );
        }
    }

}
