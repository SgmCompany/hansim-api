package com.sgm.hansimapi.domain.riot;

public class LeagueEntry {

    private final QueueType queueType;
    private final String tier;
    private final String rank;
    private final int leaguePoints;
    private final int wins;
    private final int losses;

    public LeagueEntry(QueueType queueType, String tier, String rank, int leaguePoints, int wins, int losses) {
        this.queueType = queueType;
        this.tier = tier;
        this.rank = rank;
        this.leaguePoints = leaguePoints;
        this.wins = wins;
        this.losses = losses;
    }

    public double getWinRate() {
        int total = wins + losses;
        return total == 0 ? 0.0 : (double) wins / total * 100.0;
    }

    public QueueType getQueueType() { return queueType; }
    public String getTier()         { return tier; }
    public String getRank()         { return rank; }
    public int getLeaguePoints()    { return leaguePoints; }
    public int getWins()            { return wins; }
    public int getLosses()          { return losses; }
}
