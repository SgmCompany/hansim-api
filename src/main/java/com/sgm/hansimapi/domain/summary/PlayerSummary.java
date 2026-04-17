package com.sgm.hansimapi.domain.summary;

import lombok.Getter;

@Getter
public class PlayerSummary {

    private final String name;
    private final QueueStat normal;
    private final QueueStat solo;
    private final QueueStat flex;

    public PlayerSummary(String name, QueueStat normal, QueueStat solo, QueueStat flex) {
        this.name = name;
        this.normal = normal;
        this.solo = solo;
        this.flex = flex;
    }
}
