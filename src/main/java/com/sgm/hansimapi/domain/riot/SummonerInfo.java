package com.sgm.hansimapi.domain.riot;

public class SummonerInfo {

    private final int profileIconId;
    private final long summonerLevel;

    public SummonerInfo(int profileIconId, long summonerLevel) {
        this.profileIconId = profileIconId;
        this.summonerLevel = summonerLevel;
    }

    public int getProfileIconId()  { return profileIconId; }
    public long getSummonerLevel() { return summonerLevel; }
}
