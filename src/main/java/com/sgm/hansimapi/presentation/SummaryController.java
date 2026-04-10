package com.sgm.hansimapi.presentation;

import com.sgm.hansimapi.application.summary.command.SummaryCommand;
import com.sgm.hansimapi.application.summary.usecase.SummaryUseCase;
import com.sgm.hansimapi.domain.Summary;
import com.sgm.hansimapi.presentation.dto.request.SummaryRequestQuery;
import com.sgm.hansimapi.presentation.dto.response.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hansim")
public class SummaryController {

    private final SummaryUseCase summaryUseCase;

    @GetMapping("/summary/{riotId}")
    public SummaryResponse getSummary(
            @PathVariable String riotId,
            SummaryRequestQuery query
    ) {
        SummaryCommand command = SummaryCommand.from(riotId, query);
        Summary summary = summaryUseCase.execute(command);
        return SummaryResponse.from(summary);
    }
}
