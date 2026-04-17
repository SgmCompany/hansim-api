package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.riot.Match;
import lombok.Getter;

import java.util.List;

@Getter
public class QueueStat {

    private final int games;
    private final int win;
    private final int lose;
    private final int hansimScore;
    private final double kda;

    public QueueStat(int games, int win, int lose, int hansimScore, double kda) {
        this.games = games;
        this.win = win;
        this.lose = lose;
        this.hansimScore = hansimScore;
        this.kda = kda;
    }

    public static QueueStat from(List<Match> matches) {
        int games = matches.size();
        int win = (int) matches.stream().filter(Match::isWin).count();
        int lose = games - win;

        double kda = matches.stream()
                .mapToDouble(m -> (m.getKills() + m.getAssists()) / Math.max(1.0, m.getDeaths()))
                .average()
                .orElse(0.0);

        return new QueueStat(games, win, lose, 0, kda);
    }

    public double getWinRate() {
        return games == 0 ? 0.0 : (double) win / games * 100.0;
    }
}
