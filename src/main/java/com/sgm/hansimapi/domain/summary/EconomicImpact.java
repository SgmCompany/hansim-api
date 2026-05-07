package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.user.WorkType;
import lombok.Getter;

/**
 * 호출자(로그인 유저)의 급여 정보를 기반으로 계산된 경제적 손실 추산.
 * 피검색 소환사가 아닌, 조회를 요청한 유저의 급여를 기준으로 합니다.
 */
@Getter
public class EconomicImpact {

    /** 계산에 사용된 시급 (원/시) */
    private final int hourlyRate;

    /** 경제적 손실 추산 (원) = hourlyRate × totalPlaySeconds / 3600 */
    private final long totalCost;

    /** 시급 산정 근거 */
    private final Basis basis;

    public enum Basis {
        /** 유저가 등록한 급여 정보에서 환산 */
        REGISTERED_SALARY,
        /** 미등록 또는 소득 미수집 유형 → 최저시급 적용 */
        MINIMUM_WAGE
    }

    private EconomicImpact(int hourlyRate, long totalCost, Basis basis) {
        this.hourlyRate = hourlyRate;
        this.totalCost  = totalCost;
        this.basis      = basis;
    }

    public static EconomicImpact of(int totalPlaySeconds, WorkType workType, Integer salaryAmount) {
        boolean hasRegisteredSalary = workType != null && workType.collectsIncome() && salaryAmount != null;

        int rate = HourlyRateCalculator.calculate(workType, salaryAmount);
        long cost = (long) rate * totalPlaySeconds / 3_600;
        Basis basis = hasRegisteredSalary ? Basis.REGISTERED_SALARY : Basis.MINIMUM_WAGE;

        return new EconomicImpact(rate, cost, basis);
    }
}
