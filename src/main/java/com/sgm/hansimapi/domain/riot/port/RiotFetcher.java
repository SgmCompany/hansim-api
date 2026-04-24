package com.sgm.hansimapi.domain.riot.port;

import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.SummonerInfo;

import java.util.List;

public interface RiotFetcher {

    /* puuid 조회 */
    String fetchPuuid(String gameName, String tagLine);

    /* 소환사 정보 조회 (profileIconId, summonerLevel) */
    SummonerInfo fetchSummoner(String puuid);

    /* 솔로/자유 랭크 정보 조회 */
    List<LeagueEntry> fetchLeagueEntries(String puuid);

    /* 유저의 매치 결과 */
    List<Match> fetchMatches(String puuid, long start, long end);
}
