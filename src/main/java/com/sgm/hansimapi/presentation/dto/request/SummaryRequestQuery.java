package com.sgm.hansimapi.presentation.dto.request;

import com.sgm.hansimapi.domain.summary.TimeWindow;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Setter;

@Schema(description = "한심 summary 조회 쿼리 파라미터")
// Spring MVC @ModelAttribute 바인딩을 위해 @Setter 필요
@Setter
public class SummaryRequestQuery {

    @Schema(
            description = "조회 시작일. 미입력 시 오늘(KST)로 설정",
            type = "string",
            format = "date",
            pattern = "^\\d{4}-\\d{2}-\\d{2}$",
            example = "2026-04-01",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String startDate;

    @Schema(
            description = "조회 종료일. 미입력 시 오늘(KST)로 설정. startDate 이후여야 하며 최대 7일 범위",
            type = "string",
            format = "date",
            pattern = "^\\d{4}-\\d{2}-\\d{2}$",
            example = "2026-04-10",
            nullable = true,
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String endDate;

    public TimeWindow toTimeWindow() {
        return TimeWindow.from(startDate, endDate);
    }

    public String getStartDate() { return startDate; }
    public String getEndDate()   { return endDate; }
}