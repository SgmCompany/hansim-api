package com.sgm.hansimapi.presentation.dto.request;

import com.sgm.hansimapi.domain.user.WorkType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프로필 업데이트 요청")
public record UpdateProfileRequest(

        @Schema(
                description = """
                        근무 유형.
                        - NINE_TO_SIX: 9 to 6 직장인 (연봉 선택)
                        - FAIR_24H: 교대/야간 근무 (연봉 선택)
                        - PART_TIME: 시간제 알바 (시급 선택)
                        - STUDENT: 학생 — 고등학생/대학생 (소득 미수집)
                        - UNEMPLOYED: 백수/휴직자/프리랜서 (소득 미수집)
                        """,
                example = "NINE_TO_SIX",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "근무 유형은 필수입니다.")
        WorkType workType,

        @Schema(
                description = """
                        급여 (원 단위, 선택사항).
                        - NINE_TO_SIX / FAIR_24H: 세전 연봉. 예: 50000000 (5천만원)
                        - PART_TIME: 시급. 예: 10030
                        - STUDENT / UNEMPLOYED: 무시됨
                        """,
                example = "50000000",
                nullable = true
        )
        @Min(value = 0, message = "급여는 0 이상이어야 합니다.")
        Integer salaryAmount
) {}
