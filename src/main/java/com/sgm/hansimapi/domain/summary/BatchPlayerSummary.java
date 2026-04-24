package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.QueueType;
import com.sgm.hansimapi.domain.riot.SummonerInfo;

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
    private final Streak streak;
    private final List<ChampionStat> topChampions;

    private BatchPlayerSummary(String riotId, SummonerInfo summonerInfo, List<LeagueEntry> leagueEntries,
                                QueueStat normal, QueueStat solo, QueueStat flex,
                                Streak streak, List<ChampionStat> topChampions) {
        this.riotId = riotId;
        this.summonerInfo = summonerInfo;
        this.leagueEntries = leagueEntries;
        this.normal = normal;
        this.solo = solo;
        this.flex = flex;
        this.streak = streak;
        this.topChampions = topChampions;
    }

    public static BatchPlayerSummary from(String riotId, SummonerInfo summonerInfo,
                                          List<LeagueEntry> leagueEntries, List<Match> matches) {
        Map<QueueType, List<Match>> byQueue = matches.stream()
                .collect(Collectors.groupingBy(Match::getQueueType));

        QueueStat normal = toStatOrNull(byQueue, QueueType.NORMAL);
        QueueStat solo   = toStatOrNull(byQueue, QueueType.SOLO);
        QueueStat flex   = toStatOrNull(byQueue, QueueType.FLEX);

        Streak streak = Streak.from(matches);
        List<ChampionStat> topChampions = ChampionStat.topFrom(matches);

        return new BatchPlayerSummary(riotId, summonerInfo, leagueEntries, normal, solo, flex, streak, topChampions);
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
    public Streak getStreak()                     { return streak; }
    public List<ChampionStat> getTopChampions()  { return topChampions; }
}
