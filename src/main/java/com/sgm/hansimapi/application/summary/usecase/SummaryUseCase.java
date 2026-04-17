package com.sgm.hansimapi.application.summary.usecase;

import com.sgm.hansimapi.application.summary.command.SummaryCommand;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.sgm.hansimapi.domain.summary.Summary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryUseCase {

    private final RiotFetcher riotFetcher;

    public Summary execute(SummaryCommand command) {
        String puuid = riotFetcher.fetchPuuid(command.getGameName(), command.getTagLine());

        List<Match> matches = riotFetcher.fetchMatches(
                puuid,
                command.getTimeWindow().getStart(),
                command.getTimeWindow().getEnd()
        );

        return Summary.from(matches, command);
    }
}
