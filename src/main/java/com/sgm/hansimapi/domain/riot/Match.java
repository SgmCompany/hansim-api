package com.sgm.hansimapi.domain.riot;

public class Match {

    private final boolean win;
    private final int kills;
    private final int deaths;
    private final int assists;
    private final QueueType queueType;
    private final int championId;
    private final String championName;

    public Match(boolean win, int kills, int deaths, int assists, QueueType queueType,
                 int championId, String championName) {
        this.win = win;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.queueType = queueType;
        this.championId = championId;
        this.championName = championName;
    }

    public boolean isWin()          { return win; }
    public int getKills()           { return kills; }
    public int getDeaths()          { return deaths; }
    public int getAssists()         { return assists; }
    public QueueType getQueueType() { return queueType; }
    public int getChampionId()      { return championId; }
    public String getChampionName() { return championName; }
}
