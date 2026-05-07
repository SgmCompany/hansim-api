package com.sgm.hansimapi.domain.user;

public enum WorkType {

    /** 9to6 직장인 — 심야/주말 가중치 반영, 연봉 선택 */
    NINE_TO_SIX,

    /** 교대/야간 근무 — 시간대 편향 제거, 연봉 선택 */
    FAIR_24H,

    /** 시간제 알바 — 시간대 편향 제거, 시급 선택 */
    PART_TIME,

    /** 학생 (고등학생/대학생) — 시간대 편향 제거, 소득 미수집 */
    STUDENT,

    /** 백수/휴직자/프리랜서 — 시간대 편향 제거, 소득 미수집 */
    UNEMPLOYED;

    /** NINE_TO_SIX 알고리즘 적용 여부 */
    public boolean isNineToSix() {
        return this == NINE_TO_SIX;
    }

    /** 소득 정보를 수집하는 유형 여부 */
    public boolean collectsIncome() {
        return this == NINE_TO_SIX || this == FAIR_24H || this == PART_TIME;
    }

    /** 시급 기반 유형 여부 */
    public boolean usesHourlyWage() {
        return this == PART_TIME;
    }

    /** 연봉 기반 유형 여부 */
    public boolean usesAnnualSalary() {
        return this == NINE_TO_SIX || this == FAIR_24H;
    }
}
