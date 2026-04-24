package com.sgm.hansimapi.infra.riot;

import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.QueueType;
import com.sgm.hansimapi.domain.riot.SummonerInfo;
import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RiotFetcherImpl implements RiotFetcher {

    private final RestTemplate restTemplate;

    // Riot API: 20 req/s 제한
    // Guava RateLimiter는 rate × 1s 만큼 초기 버스트를 허용하므로,
    // 첫 1초 최대 호출 = burst(R) + sustained(R) = 2R ≤ 20 → R ≤ 10
    // 안전 마진 포함 8/s 설정
    private final RateLimiter rateLimiter = RateLimiter.create(7.0);

    private static final String ASIA_ENDPOINT = "https://asia.api.riotgames.com";
    private static final String KR_ENDPOINT   = "https://kr.api.riotgames.com";

    private static final String ACCOUNT_URL      = ASIA_ENDPOINT + "/riot/account/v1/accounts/by-riot-id/%s/%s";
    private static final String MATCH_IDS_URL    = ASIA_ENDPOINT + "/lol/match/v5/matches/by-puuid/%s/ids?startTime=%d&endTime=%d&count=100";
    private static final String MATCH_DETAIL_URL = ASIA_ENDPOINT + "/lol/match/v5/matches/%s";
    private static final String SUMMONER_URL     = KR_ENDPOINT + "/lol/summoner/v4/summoners/by-puuid/%s";
    private static final String LEAGUE_URL       = KR_ENDPOINT + "/lol/league/v4/entries/by-puuid/%s";

    @Value("${riot.api.key}")
    private String apiKey;

    @Override
    public String fetchPuuid(String gameName, String tagLine) {
        String url = String.format(ACCOUNT_URL, gameName, tagLine);
        Map response = get(url, Map.class);

        if (response == null || response.get("puuid") == null) {
            throw new RuntimeException("Failed to fetch puuid for " + gameName + "#" + tagLine);
        }
        return response.get("puuid").toString();
    }

    @Override
    public SummonerInfo fetchSummoner(String puuid) {
        String url = String.format(SUMMONER_URL, puuid);
        Map response = get(url, Map.class);

        if (response == null || response.get("profileIconId") == null) {
            throw new RuntimeException(
                    "Failed to fetch summoner for puuid=" + puuid + ", response=" + response);
        }

        int profileIconId  = ((Number) response.get("profileIconId")).intValue();
        long summonerLevel = ((Number) response.get("summonerLevel")).longValue();

        return new SummonerInfo(profileIconId, summonerLevel);
    }

    @Override
    public List<LeagueEntry> fetchLeagueEntries(String summonerId) {
        String url = String.format(LEAGUE_URL, summonerId);
        List<Map> response = get(url, List.class);

        if (response == null) return List.of();

        List<LeagueEntry> entries = new ArrayList<>();
        for (Map entry : response) {
            String queueTypeStr = (String) entry.get("queueType");
            QueueType queueType = switch (queueTypeStr) {
                case "RANKED_SOLO_5x5" -> QueueType.SOLO;
                case "RANKED_FLEX_SR"  -> QueueType.FLEX;
                default -> null;
            };
            if (queueType == null) continue;

            String tier    = (String) entry.get("tier");
            String rank    = (String) entry.get("rank");
            int lp         = (int)    entry.get("leaguePoints");
            int wins       = (int)    entry.get("wins");
            int losses     = (int)    entry.get("losses");

            entries.add(new LeagueEntry(queueType, tier, rank, lp, wins, losses));
        }
        return entries;
    }

    @Override
    public List<Match> fetchMatches(String puuid, long start, long end) {
        String url = String.format(MATCH_IDS_URL, puuid, start / 1000, end / 1000);
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

    private static final int MAX_RETRIES = 2;

    private <T> T get(String url, Class<T> responseType) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            rateLimiter.acquire();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Riot-Token", apiKey);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            try {
                return restTemplate.exchange(url, HttpMethod.GET, entity, responseType).getBody();
            } catch (HttpClientErrorException.TooManyRequests e) {
                String retryAfter = e.getResponseHeaders() != null
                        ? e.getResponseHeaders().getFirst("Retry-After") : null;
                long waitSec = retryAfter != null ? Long.parseLong(retryAfter) : 5L;
                log.warn("[RiotAPI] 429 수신 (attempt {}/{}) | Retry-After: {}s | url: {}",
                        attempt + 1, MAX_RETRIES + 1, waitSec, url.replaceAll("https://[^/]+", ""));
                if (attempt == MAX_RETRIES) throw e;
                try {
                    TimeUnit.SECONDS.sleep(waitSec);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw new IllegalStateException("unreachable");
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
                boolean win      = (boolean) p.get("win");
                int kills        = (int)     p.get("kills");
                int deaths       = (int)     p.get("deaths");
                int assists      = (int)     p.get("assists");
                int championId   = (int)     p.get("championId");
                String championName = (String) p.get("championName");
                return new Match(win, kills, deaths, assists, queueType, championId, championName);
            }
        }
        return null;
    }
}
