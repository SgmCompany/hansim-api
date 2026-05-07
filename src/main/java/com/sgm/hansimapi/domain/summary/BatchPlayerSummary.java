package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.QueueType;
import com.sgm.hansimapi.domain.riot.SummonerInfo;
import com.sgm.hansimapi.domain.user.WorkType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BatchPlayerSummary {

    private final String riotId;
    private final SummonerInfo summonerInfo;
    private final List<LeagueEntry> leagueEntries;
    private final QueueStat normal;
    private final QueueStat solo;
    private final QueueStat flex;
    private final QueueStat aram;
    private final Streak streak;
    private final List<ChampionStat> topChampions;
    private final HlsResult hls;

    /** 전체 큐 합산 총 플레이 시간 (초) */
    private final int totalPlaySeconds;

    /** 호출자 급여 기준 경제적 손실 추산 */
    private final EconomicImpact economicImpact;

    private BatchPlayerSummary(String riotId, SummonerInfo summonerInfo, List<LeagueEntry> leagueEntries,
                                QueueStat normal, QueueStat solo, QueueStat flex, QueueStat aram,
                                Streak streak, List<ChampionStat> topChampions, HlsResult hls,
                                int totalPlaySeconds, EconomicImpact economicImpact) {
        this.riotId = riotId;
        this.summonerInfo = summonerInfo;
        this.leagueEntries = leagueEntries;
        this.normal = normal;
        this.solo = solo;
        this.flex = flex;
        this.aram = aram;
        this.streak = streak;
        this.topChampions = topChampions;
        this.hls = hls;
        this.totalPlaySeconds = totalPlaySeconds;
        this.economicImpact = economicImpact;
    }

    public static BatchPlayerSummary from(String riotId, SummonerInfo summonerInfo,
                                          List<LeagueEntry> leagueEntries, List<Match> matches,
                                          WorkType callerWorkType, Integer callerSalaryAmount) {
        Map<QueueType, List<Match>> byQueue = matches.stream()
                .collect(Collectors.groupingBy(Match::getQueueType));

        QueueStat normal = toStatOrNull(byQueue, QueueType.NORMAL);
        QueueStat solo   = toStatOrNull(byQueue, QueueType.SOLO);
        QueueStat flex   = toStatOrNull(byQueue, QueueType.FLEX);
        QueueStat aram   = toStatOrNull(byQueue, QueueType.ARAM);

        Streak streak = Streak.from(matches);
        List<ChampionStat> topChampions = ChampionStat.topFrom(matches);
        HlsResult hls = HlsCalculator.calculate(matches);
        int totalPlaySeconds = matches.stream().mapToInt(Match::getGameDuration).sum();
        EconomicImpact economicImpact = EconomicImpact.of(totalPlaySeconds, callerWorkType, callerSalaryAmount);

        return new BatchPlayerSummary(riotId, summonerInfo, leagueEntries, normal, solo, flex, aram,
                streak, topChampions, hls, totalPlaySeconds, economicImpact);
    }

    private static QueueStat toStatOrNull(Map<QueueType, List<Match>> byQueue, QueueType type) {
        List<Match> matches = byQueue.get(type);
        return (matches == null || matches.isEmpty()) ? null : QueueStat.from(matches);
    }

    public LeagueEntry getLeagueEntry(QueueType queueType) {
        return leagueEntries.stream()
                .filter(e -> e.getQueueType() == queueType)
                .findFirst()
                .orElse(null);
    }

    public String getRiotId()                    { return riotId; }
    public SummonerInfo getSummonerInfo()         { return summonerInfo; }
    public List<LeagueEntry> getLeagueEntries()  { return leagueEntries; }
    public QueueStat getNormal()                  { return normal; }
    public QueueStat getSolo()                    { return solo; }
    public QueueStat getFlex()                    { return flex; }
    public QueueStat getAram()                    { return aram; }
    public Streak getStreak()                     { return streak; }
    public List<ChampionStat> getTopChampions()  { return topChampions; }
    public HlsResult getHls()                    { return hls; }
    public int getTotalPlaySeconds()             { return totalPlaySeconds; }
    public EconomicImpact getEconomicImpact()    { return economicImpact; }
}
