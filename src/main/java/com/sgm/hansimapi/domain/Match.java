package com.sgm.hansimapi.domain;

public class Match {

    private final boolean win;
    private final int kills;
    private final int deaths;
    private final int assists;
    private final QueueType queueType;

    public Match(boolean win, int kills, int deaths, int assists, QueueType queueType) {
        this.win = win;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.queueType = queueType;
    }

    public boolean isWin() { return win; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getAssists() { return assists; }
    public QueueType getQueueType() { return queueType; }
}
