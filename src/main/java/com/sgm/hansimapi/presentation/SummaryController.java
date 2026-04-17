package com.sgm.hansimapi.presentation;

import com.sgm.hansimapi.application.summary.command.SummaryCommand;
import com.sgm.hansimapi.application.summary.usecase.SummaryUseCase;
import com.sgm.hansimapi.domain.Summary;
import com.sgm.hansimapi.presentation.dto.request.SummaryRequestQuery;
import com.sgm.hansimapi.presentation.dto.response.SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Summary", description = "한심 지수 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hansim")
public class SummaryController {

    private final SummaryUseCase summaryUseCase;

    @Operation(summary = "한심 summary 조회", description = "Riot ID 기준으로 지정 기간의 큐별 승패 및 한심 지수를 조회합니다.")
    @GetMapping("/summary/{riotId}")
    public SummaryResponse getSummary(
            @Parameter(
                    description = "Riot ID. 반드시 '이름-태그' 슬러그 형식 (예: 페이커-KR1). '#' 구분자는 허용하지 않음",
                    required = true,
                    example = "페이커-KR1",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", maxLength = 50)
            ) @PathVariable String riotId,
            SummaryRequestQuery query
    ) {
        SummaryCommand command = SummaryCommand.from(riotId, query);
        Summary summary = summaryUseCase.execute(command);
        return SummaryResponse.from(summary);
    }
}
