package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.Match;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChampionStat {

    private final int championId;
    private final String championName;
    private final int games;
    private final int wins;
    private final double kda;

    public ChampionStat(int championId, String championName, int games, int wins, double kda) {
        this.championId = championId;
        this.championName = championName;
        this.games = games;
        this.wins = wins;
        this.kda = kda;
    }

    public static List<ChampionStat> topFrom(List<Match> matches) {
        Map<String, List<Match>> byChampion = matches.stream()
                .collect(Collectors.groupingBy(Match::getChampionName));

        return byChampion.entrySet().stream()
                .map(e -> {
                    List<Match> cm = e.getValue();
                    int games = cm.size();
                    int wins  = (int) cm.stream().filter(Match::isWin).count();
                    double kda = cm.stream()
                            .mapToDouble(m -> (m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths()))
                            .average()
                            .orElse(0.0);
                    int championId = cm.get(0).getChampionId();
                    return new ChampionStat(championId, e.getKey(), games, wins, kda);
                })
                .sorted(Comparator.comparingInt(ChampionStat::getGames).reversed())
                .collect(Collectors.toList());
    }

    public int getChampionId()      { return championId; }
    public String getChampionName() { return championName; }
    public int getGames()           { return games; }
    public int getWins()            { return wins; }
    public double getKda()          { return kda; }
}
