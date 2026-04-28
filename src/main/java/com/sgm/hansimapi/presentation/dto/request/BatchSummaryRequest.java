package com.sgm.hansimapi.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "다중 소환사 한심 지수 조회 요청")
public record BatchSummaryRequest(

        @Schema(
                description = "조회할 Riot ID 목록. '이름#태그' 또는 '이름-태그' 형식 모두 허용. 최소 1명 최대 10명",
                example = "[\"페이커#KR1\", \"카나비#KR1\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotEmpty
        @Size(min = 1, max = 10, message = "소환사는 1명 이상 10명 이하로 입력해주세요.")
        List<@NotBlank(message = "Riot ID는 공백일 수 없습니다.") String> riotIds,

        @Schema(
                description = "조회 시작일 (yyyy-MM-dd). 미입력 시 오늘(KST)",
                type = "string",
                format = "date",
                example = "2026-04-01",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String startDate,

        @Schema(
                description = "조회 종료일 (yyyy-MM-dd). 미입력 시 오늘(KST). 최대 7일 범위",
                type = "string",
                format = "date",
                example = "2026-04-10",
                nullable = true,
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        String endDate
) {}
