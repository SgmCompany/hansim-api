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

    public PlayerSummary(String name, QueueStat normal, QueueStat solo, QueueStat flex,
                         QueueStat aram, HlsResult hls) {
        this.name   = name;
        this.normal = normal;
        this.solo   = solo;
        this.flex   = flex;
        this.aram   = aram;
        this.hls    = hls;
    }
}
