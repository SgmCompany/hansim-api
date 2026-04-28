package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.Match;
import lombok.Getter;

import java.util.List;

@Getter
public class QueueStat {

    private final int games;
    private final int win;
    private final int lose;
    private final double kda;

    // CS
    private final double avgCsPerMin;

    // 딜량
    private final int avgDamage;

    // 비전
    private final double avgVisionScore;

    // 멀티킬 합산
    private final int totalDoubleKills;
    private final int totalTripleKills;
    private final int totalQuadraKills;
    private final int totalPentaKills;

    public QueueStat(int games, int win, int lose, double kda,
                     double avgCsPerMin, int avgDamage, double avgVisionScore,
                     int totalDoubleKills, int totalTripleKills, int totalQuadraKills, int totalPentaKills) {
        this.games = games;
        this.win = win;
        this.lose = lose;
        this.kda = kda;
        this.avgCsPerMin = avgCsPerMin;
        this.avgDamage = avgDamage;
        this.avgVisionScore = avgVisionScore;
        this.totalDoubleKills = totalDoubleKills;
        this.totalTripleKills = totalTripleKills;
        this.totalQuadraKills = totalQuadraKills;
        this.totalPentaKills = totalPentaKills;
    }

    public static QueueStat from(List<Match> matches) {
        int games = matches.size();
        int win   = (int) matches.stream().filter(Match::isWin).count();
        int lose  = games - win;

        double kda = matches.stream()
                .mapToDouble(m -> (m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths()))
                .average()
                .orElse(0.0);

        double avgCsPerMin = matches.stream()
                .mapToDouble(Match::getCsPerMin)
                .average()
                .orElse(0.0);

        int avgDamage = (int) matches.stream()
                .mapToInt(Match::getTotalDamageDealtToChampions)
                .average()
                .orElse(0.0);

        double avgVisionScore = matches.stream()
                .mapToInt(Match::getVisionScore)
                .average()
                .orElse(0.0);

        int totalDoubleKills = matches.stream().mapToInt(Match::getDoubleKills).sum();
        int totalTripleKills = matches.stream().mapToInt(Match::getTripleKills).sum();
        int totalQuadraKills = matches.stream().mapToInt(Match::getQuadraKills).sum();
        int totalPentaKills  = matches.stream().mapToInt(Match::getPentaKills).sum();

        return new QueueStat(games, win, lose, kda,
                avgCsPerMin, avgDamage, avgVisionScore,
                totalDoubleKills, totalTripleKills, totalQuadraKills, totalPentaKills);
    }

    public double getWinRate() {
        return games == 0 ? 0.0 : (double) win / games * 100.0;
    }
}
