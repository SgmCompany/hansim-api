package com.sgm.hansimapi.domain.summary;

import java.time.Instant;
import java.util.List;

public class BatchSummary {

    private final TimeWindow timeWindow;
    private final List<BatchPlayerSummary> players;

    public BatchSummary(TimeWindow timeWindow, List<BatchPlayerSummary> players) {
        this.timeWindow = timeWindow;
        this.players = players;
    }

    public Instant getStart()                        { return Instant.ofEpochMilli(timeWindow.getStart()); }
    public Instant getEnd()                          { return Instant.ofEpochMilli(timeWindow.getEnd()); }
    public String getPeriodStr()                     { return timeWindow.getPeriodStr(); }
    public List<BatchPlayerSummary> getPlayers()     { return players; }
}
