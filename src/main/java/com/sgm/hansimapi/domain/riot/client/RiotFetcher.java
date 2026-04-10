package com.sgm.hansimapi.domain.riot.client;

import com.sgm.hansimapi.domain.Match;

import java.util.List;

public interface RiotFetcher {

    /* 유저 조회 */
    String fetchPuuid(String gameName, String tagLine);

    /* 유저의 매치 결과*/
    List<Match> fetchMatches(String puuid, long start, long end);
}