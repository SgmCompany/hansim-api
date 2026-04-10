package com.sgm.hansimapi.domain;

import com.sgm.hansimapi.application.summary.command.SummaryCommand;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Summary {

    private final TimeWindow timeWindow;
    private final List<PlayerSummary> players;

    private Summary(TimeWindow timeWindow, List<PlayerSummary> players) {
        this.timeWindow = timeWindow;
        this.players = players;
    }

    public static Summary from(List<Match> matches, SummaryCommand command) {
        Map<QueueType, List<Match>> byQueue = matches.stream()
                .collect(Collectors.groupingBy(Match::getQueueType));

        QueueStat normal = toStatOrNull(byQueue, QueueType.NORMAL);
        QueueStat solo   = toStatOrNull(byQueue, QueueType.SOLO);
        QueueStat flex   = toStatOrNull(byQueue, QueueType.FLEX);

        String playerName = command.getGameName() + "#" + command.getTagLine();
        PlayerSummary player = new PlayerSummary(playerName, normal, solo, flex);

        return new Summary(command.getTimeWindow(), List.of(player));
    }

    private static QueueStat toStatOrNull(Map<QueueType, List<Match>> byQueue, QueueType type) {
        List<Match> matches = byQueue.get(type);
        return (matches == null || matches.isEmpty()) ? null : QueueStat.from(matches);
    }

    public Instant getStart() { return Instant.ofEpochMilli(timeWindow.getStart()); }
    public Instant getEnd()   { return Instant.ofEpochMilli(timeWindow.getEnd()); }
    public String getPeriodStr()          { return timeWindow.getPeriodStr(); }
    public List<PlayerSummary> getPlayers() { return players; }
}
