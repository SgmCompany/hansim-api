package com.sgm.hansimapi.application.summary.command;

import com.sgm.hansimapi.domain.TimeWindow;
import com.sgm.hansimapi.domain.riot.RiotId;
import com.sgm.hansimapi.presentation.dto.request.SummaryRequestQuery;

public class SummaryCommand {

    private final String gameName;
    private final String tagLine;
    private final TimeWindow timeWindow;

    private SummaryCommand(String gameName, String tagLine, TimeWindow timeWindow) {
        this.gameName = gameName;
        this.tagLine = tagLine;
        this.timeWindow = timeWindow;
    }

    public static SummaryCommand from(String riotId, SummaryRequestQuery query) {
        RiotId parsed = RiotId.fromSlug(riotId);
        TimeWindow timeWindow = query.toTimeWindow();
        return new SummaryCommand(parsed.getGameName(), parsed.getTagLine(), timeWindow);
    }

    public String getGameName() { return gameName; }
    public String getTagLine()  { return tagLine; }
    public TimeWindow getTimeWindow() { return timeWindow; }
}
