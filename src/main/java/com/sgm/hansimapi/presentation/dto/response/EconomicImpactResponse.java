package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.summary.EconomicImpact;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "경제적 손실 추산 (호출자 급여 기준)")
public class EconomicImpactResponse {

    @Schema(description = "적용된 시급 (원/시)", example = "24038",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final int hourlyRate;

    @Schema(description = "경제적 손실 추산 (원)", example = "72115",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final long totalCost;

    @Schema(
            description = "시급 산정 근거. REGISTERED_SALARY=등록 급여, MINIMUM_WAGE=최저시급(비로그인/미등록)",
            example = "REGISTERED_SALARY",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private final EconomicImpact.Basis basis;

    private EconomicImpactResponse(int hourlyRate, long totalCost, EconomicImpact.Basis basis) {
        this.hourlyRate = hourlyRate;
        this.totalCost  = totalCost;
        this.basis      = basis;
    }

    public static EconomicImpactResponse from(EconomicImpact impact) {
        return new EconomicImpactResponse(
                impact.getHourlyRate(),
                impact.getTotalCost(),
                impact.getBasis()
        );
    }
}
