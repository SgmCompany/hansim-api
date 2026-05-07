package com.sgm.hansimapi.domain.summary;

import lombok.Getter;

@Getter
public class PlayerSummary {

    private final String name;
    private final QueueStat normal;
    private final QueueStat solo;
    private final QueueStat flex;
    private final QueueStat aram;

    /** 전체 매치 기준으로 계산된 HLS (큐 구분 없음) */
    private final HlsResult hls;

    /** 전체 큐 합산 총 플레이 시간 (초) */
    private final int totalPlaySeconds;

    /** 호출자 급여 기준 경제적 손실 추산 */
    private final EconomicImpact economicImpact;

    public PlayerSummary(String name, QueueStat normal, QueueStat solo, QueueStat flex,
                         QueueStat aram, HlsResult hls, int totalPlaySeconds, EconomicImpact economicImpact) {
        this.name            = name;
        this.normal          = normal;
        this.solo            = solo;
        this.flex            = flex;
        this.aram            = aram;
        this.hls             = hls;
        this.totalPlaySeconds = totalPlaySeconds;
        this.economicImpact  = economicImpact;
    }
}
