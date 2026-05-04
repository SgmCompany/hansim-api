package com.sgm.hansimapi.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "소환사 연동 요청")
public record LinkSummonerRequest(

        @Schema(description = "Riot 게임 이름", example = "페이커", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "게임 이름은 필수입니다.")
        @Size(max = 100, message = "게임 이름은 100자 이하여야 합니다.")
        String gameName,

        @Schema(description = "Riot 태그라인 (# 제외)", example = "KR1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "태그라인은 필수입니다.")
        @Size(max = 20, message = "태그라인은 20자 이하여야 합니다.")
        String tagLine
) {}
