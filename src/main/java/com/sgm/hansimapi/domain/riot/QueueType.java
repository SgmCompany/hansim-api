package com.sgm.hansimapi.domain.riot;

public enum QueueType {
    NORMAL, SOLO, FLEX, ARAM;

    public static QueueType from(int queueId) {
        return switch (queueId) {
            case 420 -> SOLO;
            case 440 -> FLEX;
            case 450 -> ARAM;
            default -> NORMAL;
        };
    }
}
