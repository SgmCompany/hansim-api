package com.sgm.hansimapi.domain.summary;

import com.sgm.hansimapi.domain.user.WorkType;

public class HourlyRateCalculator {

    /** 2025년 최저시급 (원) */
    public static final int MINIMUM_WAGE = 10_030;

    /** 연봉 → 시급 환산 기준: 52주 × 40시간 = 2,080시간 */
    private static final int ANNUAL_WORK_HOURS = 2_080;

    private HourlyRateCalculator() {}

    /**
     * WorkType과 salaryAmount(원 단위)를 받아 시급(원/시)을 반환합니다.
     * <ul>
     *   <li>PART_TIME: salaryAmount 자체가 시급</li>
     *   <li>NINE_TO_SIX / FAIR_24H: 연봉 ÷ 2,080</li>
     *   <li>그 외(STUDENT, UNEMPLOYED) 또는 미등록: 최저시급 반환</li>
     * </ul>
     */
    public static int calculate(WorkType workType, Integer salaryAmount) {
        if (workType == null || salaryAmount == null) {
            return MINIMUM_WAGE;
        }
        if (workType.usesHourlyWage()) {
            return salaryAmount;
        }
        if (workType.usesAnnualSalary()) {
            return salaryAmount / ANNUAL_WORK_HOURS;
        }
        return MINIMUM_WAGE;
    }
}
