package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.PlayerSummary;
import com.sgm.hansimapi.domain.QueueStat;
import com.sgm.hansimapi.domain.Summary;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class SummaryResponse {

    private final Period period;
    private final String periodStr;
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
    static class Period {
        private final String start;
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
    static class Player {
        private final String name;
        private final Queue normal;
        private final Queue solo;
        private final Queue flex;

        private Player(String name, Queue normal, Queue solo, Queue flex) {
            this.name = name;
            this.normal = normal;
            this.solo = solo;
            this.flex = flex;
        }

        public static Player from(PlayerSummary player) {
            return new Player(
                    player.getName(),
                    Queue.from(player.getNormal()),
                    Queue.from(player.getSolo()),
                    Queue.from(player.getFlex())
            );
        }
    }

    @Getter
    static class Queue {
        private final int games;
        private final int win;
        private final int lose;
        private final int hansimScore;
        private final String kda;

        private Queue(int games, int win, int lose, int hansimScore, String kda) {
            this.games = games;
            this.win = win;
            this.lose = lose;
            this.hansimScore = hansimScore;
            this.kda = kda;
        }

        public static Queue from(QueueStat stat) {
            if (stat == null) return null;

            return new Queue(
                    stat.getGames(),
                    stat.getWin(),
                    stat.getLose(),
                    stat.getHansimScore(),
                    String.format("%.1f", stat.getKda())
            );
        }
    }
}