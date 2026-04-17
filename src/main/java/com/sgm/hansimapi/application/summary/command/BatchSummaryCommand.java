package com.sgm.hansimapi.application.summary.command;

import com.sgm.hansimapi.domain.summary.TimeWindow;
import com.sgm.hansimapi.domain.riot.RiotId;

import java.util.List;

public class BatchSummaryCommand {

    private final List<RiotId> riotIds;
    private final TimeWindow timeWindow;

    private BatchSummaryCommand(List<RiotId> riotIds, TimeWindow timeWindow) {
        this.riotIds = riotIds;
        this.timeWindow = timeWindow;
    }

    public static BatchSummaryCommand of(List<String> riotIdSlugs, String startDate, String endDate) {
        List<RiotId> riotIds = riotIdSlugs.stream()
                .map(RiotId::from)
                .toList();
        TimeWindow timeWindow = TimeWindow.from(startDate, endDate);
        return new BatchSummaryCommand(riotIds, timeWindow);
    }

    public List<RiotId> getRiotIds()       { return riotIds; }
    public TimeWindow getTimeWindow()      { return timeWindow; }
}
