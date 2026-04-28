package com.sgm.hansimapi.domain.riot;

public class Match {

    private final boolean win;
    private final int kills;
    private final int deaths;
    private final int assists;
    private final QueueType queueType;
    private final int championId;
    private final String championName;

    // 포지션
    private final String teamPosition;

    // CS
    private final int totalMinionsKilled;
    private final int neutralMinionsKilled;

    // 게임 시간 (초)
    private final int gameDuration;

    // 딜량
    private final int totalDamageDealtToChampions;

    // 비전
    private final int visionScore;
    private final int wardsPlaced;
    private final int wardsKilled;

    // 멀티킬
    private final int doubleKills;
    private final int tripleKills;
    private final int quadraKills;
    private final int pentaKills;

    /** 게임 시작 시각 (Unix epoch ms) — Streak 정렬 기준 */
    private final long gameStart;

    /** 일반 서렌 종료 여부 — HLS 결과 가중치에 사용 */
    private final boolean gameEndedInSurrender;

    /** 20분 이전 얼리 서렌 여부 — HLS 결과 가중치에 사용 */
    private final boolean gameEndedInEarlySurrender;

    public Match(boolean win, int kills, int deaths, int assists,
                 QueueType queueType, int championId, String championName,
                 String teamPosition,
                 int totalMinionsKilled, int neutralMinionsKilled, int gameDuration,
                 int totalDamageDealtToChampions,
                 int visionScore, int wardsPlaced, int wardsKilled,
                 int doubleKills, int tripleKills, int quadraKills, int pentaKills,
                 long gameStart,
                 boolean gameEndedInSurrender, boolean gameEndedInEarlySurrender) {
        this.win = win;
        this.kills = kills;
        this.deaths = deaths;
        this.assists = assists;
        this.queueType = queueType;
        this.championId = championId;
        this.championName = championName;
        this.teamPosition = teamPosition;
        this.totalMinionsKilled = totalMinionsKilled;
        this.neutralMinionsKilled = neutralMinionsKilled;
        this.gameDuration = gameDuration;
        this.totalDamageDealtToChampions = totalDamageDealtToChampions;
        this.visionScore = visionScore;
        this.wardsPlaced = wardsPlaced;
        this.wardsKilled = wardsKilled;
        this.doubleKills = doubleKills;
        this.tripleKills = tripleKills;
        this.quadraKills = quadraKills;
        this.pentaKills = pentaKills;
        this.gameStart = gameStart;
        this.gameEndedInSurrender = gameEndedInSurrender;
        this.gameEndedInEarlySurrender = gameEndedInEarlySurrender;
    }

    /** 분당 CS = (미니언 + 중립 몬스터) / 게임 시간(분) */
    public double getCsPerMin() {
        if (gameDuration <= 0) return 0.0;
        return (totalMinionsKilled + neutralMinionsKilled) / (gameDuration / 60.0);
    }

    public boolean isWin()                          { return win; }
    public int getKills()                           { return kills; }
    public int getDeaths()                          { return deaths; }
    public int getAssists()                         { return assists; }
    public QueueType getQueueType()                 { return queueType; }
    public int getChampionId()                      { return championId; }
    public String getChampionName()                 { return championName; }
    public String getTeamPosition()                 { return teamPosition; }
    public int getTotalMinionsKilled()              { return totalMinionsKilled; }
    public int getNeutralMinionsKilled()            { return neutralMinionsKilled; }
    public int getGameDuration()                    { return gameDuration; }
    public int getTotalDamageDealtToChampions()     { return totalDamageDealtToChampions; }
    public int getVisionScore()                     { return visionScore; }
    public int getWardsPlaced()                     { return wardsPlaced; }
    public int getWardsKilled()                     { return wardsKilled; }
    public int getDoubleKills()                     { return doubleKills; }
    public int getTripleKills()                     { return tripleKills; }
    public int getQuadraKills()                     { return quadraKills; }
    public int getPentaKills()                      { return pentaKills; }
    public long getGameStart()                      { return gameStart; }
    public boolean isGameEndedInSurrender()         { return gameEndedInSurrender; }
    public boolean isGameEndedInEarlySurrender()    { return gameEndedInEarlySurrender; }
}
