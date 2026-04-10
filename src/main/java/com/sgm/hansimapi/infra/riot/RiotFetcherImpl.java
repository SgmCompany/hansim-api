package com.sgm.hansimapi.infra.riot;

import com.sgm.hansimapi.domain.Match;
import com.sgm.hansimapi.domain.QueueType;
import com.sgm.hansimapi.domain.riot.client.RiotFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RiotFetcherImpl implements RiotFetcher {

    private final RestTemplate restTemplate;

    private static final String RIOT_ENDPOINT    = "https://asia.api.riotgames.com";

    @Value("${riot.api.key}")
    private String apiKey;

    private static final String ACCOUNT_URL      = RIOT_ENDPOINT + "/riot/account/v1/accounts/by-riot-id/%s/%s";
    private static final String MATCH_IDS_URL    = RIOT_ENDPOINT + "/lol/match/v5/matches/by-puuid/%s/ids?startTime=%d&endTime=%d";
    private static final String MATCH_DETAIL_URL = RIOT_ENDPOINT + "/lol/match/v5/matches/%s";

    @Override
    public String fetchPuuid(String gameName, String tagLine) {
        String url = String.format(ACCOUNT_URL, gameName, tagLine);
        Map response = get(url, Map.class);

        if (response == null || response.get("puuid") == null) {
            throw new RuntimeException("Failed to fetch puuid");
        }
        return response.get("puuid").toString();
    }

    @Override
    public List<Match> fetchMatches(String puuid, long start, long end) {
        String url = String.format(MATCH_IDS_URL, puuid, start / 1000, end / 1000);

        System.out.println("url" + url);

        List<String> matchIds = get(url, List.class);

        if (matchIds == null || matchIds.isEmpty()) {
            return List.of();
        }

        List<Match> matches = new ArrayList<>();
        for (String matchId : matchIds) {
            Map matchResponse = get(String.format(MATCH_DETAIL_URL, matchId), Map.class);
            Match match = convertToMatch(matchResponse, puuid);
            if (match != null) {
                matches.add(match);
            }
        }
        return matches;
    }

    private <T> T get(String url, Class<T> responseType) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, entity, responseType);
        return response.getBody();
    }

    private Match convertToMatch(Map response, String puuid) {
        if (response == null) return null;

        Map info = (Map) response.get("info");
        if (info == null) return null;

        int queueId = (int) info.get("queueId");
        QueueType queueType = QueueType.from(queueId);

        List participants = (List) info.get("participants");
        for (Object obj : participants) {
            Map p = (Map) obj;
            if (puuid.equals(p.get("puuid"))) {
                boolean win   = (boolean) p.get("win");
                int kills     = (int) p.get("kills");
                int deaths    = (int) p.get("deaths");
                int assists   = (int) p.get("assists");
                return new Match(win, kills, deaths, assists, queueType);
            }
        }
        return null;
    }
}
