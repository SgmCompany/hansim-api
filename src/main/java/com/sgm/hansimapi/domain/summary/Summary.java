package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.application.summary.command.SummaryCommand;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.QueueType;
import com.sgm.hansimapi.domain.user.WorkType;

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

    public static Summary from(List<Match> matches, SummaryCommand command,
                               WorkType callerWorkType, Integer callerSalaryAmount) {
        Map<QueueType, List<Match>> byQueue = matches.stream()
                .collect(Collectors.groupingBy(Match::getQueueType));

        QueueStat normal = toStatOrNull(byQueue, QueueType.NORMAL);
        QueueStat solo   = toStatOrNull(byQueue, QueueType.SOLO);
        QueueStat flex   = toStatOrNull(byQueue, QueueType.FLEX);
        QueueStat aram   = toStatOrNull(byQueue, QueueType.ARAM);

        // HLS는 모든 큐(노말/솔로/자유/칼바람) 매치를 합산하여 계산
        HlsResult hls = HlsCalculator.calculate(matches);

        int totalPlaySeconds = matches.stream().mapToInt(Match::getGameDuration).sum();
        EconomicImpact economicImpact = EconomicImpact.of(totalPlaySeconds, callerWorkType, callerSalaryAmount);

        String playerName = command.getGameName() + "#" + command.getTagLine();
        PlayerSummary player = new PlayerSummary(playerName, normal, solo, flex, aram, hls,
                totalPlaySeconds, economicImpact);

        return new Summary(command.getTimeWindow(), List.of(player));
    }

    private static QueueStat toStatOrNull(Map<QueueType, List<Match>> byQueue, QueueType type) {
        List<Match> matches = byQueue.get(type);
        return (matches == null || matches.isEmpty()) ? null : QueueStat.from(matches);
    }

    public Instant getStart()               { return Instant.ofEpochMilli(timeWindow.getStart()); }
    public Instant getEnd()                 { return Instant.ofEpochMilli(timeWindow.getEnd()); }
    public String getPeriodStr()            { return timeWindow.getPeriodStr(); }
    public List<PlayerSummary> getPlayers() { return players; }
}
