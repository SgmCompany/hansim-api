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
    /** 가장 많이 플레이한 포지션 (TOP/JUNGLE/MID/BOTTOM/UTILITY) */
    private final String topPosition;

    public ChampionStat(int championId, String championName, int games, int wins,
                        double kda, String topPosition) {
        this.championId = championId;
        this.championName = championName;
        this.games = games;
        this.wins = wins;
        this.kda = kda;
        this.topPosition = topPosition;
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

                    // 가장 빈도 높은 포지션
                    String topPosition = cm.stream()
                            .filter(m -> m.getTeamPosition() != null && !m.getTeamPosition().isBlank())
                            .collect(Collectors.groupingBy(Match::getTeamPosition, Collectors.counting()))
                            .entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(null);

                    return new ChampionStat(championId, e.getKey(), games, wins, kda, topPosition);
                })
                .sorted(Comparator.comparingInt(ChampionStat::getGames).reversed())
                .collect(Collectors.toList());
    }

    public int getChampionId()      { return championId; }
    public String getChampionName() { return championName; }
    public int getGames()           { return games; }
    public int getWins()            { return wins; }
    public double getKda()          { return kda; }
    public String getTopPosition()  { return topPosition; }
}
