package com.sgm.hansimapi.presentation;

import com.sgm.hansimapi.application.summary.command.BatchSummaryCommand;
import com.sgm.hansimapi.application.summary.command.SummaryCommand;
import com.sgm.hansimapi.application.summary.usecase.BatchSummaryUseCase;
import com.sgm.hansimapi.application.summary.usecase.SummaryUseCase;
import com.sgm.hansimapi.domain.summary.BatchSummary;
import com.sgm.hansimapi.domain.summary.Summary;
import com.sgm.hansimapi.presentation.dto.request.BatchSummaryRequest;
import com.sgm.hansimapi.presentation.dto.request.SummaryRequestQuery;
import com.sgm.hansimapi.presentation.dto.response.BatchSummaryResponse;
import com.sgm.hansimapi.presentation.dto.response.SummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Summary", description = "한심 지수 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/hansim")
public class SummaryController {

    private final SummaryUseCase summaryUseCase;
    private final BatchSummaryUseCase batchSummaryUseCase;

    @Operation(summary = "단일 소환사 한심 summary 조회",
               description = "Riot ID 기준으로 지정 기간의 큐별 승패 및 한심 지수를 조회합니다.")
    @GetMapping("/summary/{riotId}")
    public SummaryResponse getSummary(
            @Parameter(
                    description = "Riot ID. 반드시 '이름-태그' 슬러그 형식 (예: 페이커-KR1). '#' 구분자는 허용하지 않음",
                    required = true,
                    example = "페이커-KR1",
                    schema = @io.swagger.v3.oas.annotations.media.Schema(type = "string", maxLength = 50)
            ) @PathVariable String riotId,
            SummaryRequestQuery query,
            @AuthenticationPrincipal Long userId
    ) {
        SummaryCommand command = SummaryCommand.from(riotId, query);
        Summary summary = summaryUseCase.execute(command, userId);
        return SummaryResponse.from(summary);
    }

    @Operation(summary = "다중 소환사 한심 summary 조회 (최대 10명)",
               description = "여러 소환사의 큐별 승패, 랭크 정보, 스트릭, 챔피언 통계를 한 번에 조회합니다. 비로그인/로그인 모두 사용 가능합니다.")
    @PostMapping("/summary/batch")
    public BatchSummaryResponse getBatchSummary(@Valid @RequestBody BatchSummaryRequest request,
                                                @AuthenticationPrincipal Long userId) {
        BatchSummaryCommand command = BatchSummaryCommand.of(
                request.riotIds(),
                request.startDate(),
                request.endDate()
        );
        BatchSummary summary = batchSummaryUseCase.execute(command, userId);
        return BatchSummaryResponse.from(summary);
    }
}
