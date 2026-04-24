package com.sgm.hansimapi.infra.riot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.QueueType;
import com.sgm.hansimapi.domain.riot.SummonerInfo;
import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RiotFetcherImpl implements RiotFetcher {

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redis;
    private final MatchJpaRepository matchRepo;
    private final MatchParticipantJpaRepository participantRepo;
    private final ObjectMapper objectMapper;

    // Riot API: 20 req/s 제한
    // Guava RateLimiter는 rate × 1s 만큼 초기 버스트를 허용하므로,
    // 첫 1초 최대 호출 = burst(R) + sustained(R) = 2R ≤ 20 → R ≤ 10
    // 안전 마진 포함 7/s 설정
    private final RateLimiter rateLimiter = RateLimiter.create(7.0);

    private static final String ASIA_ENDPOINT = "https://asia.api.riotgames.com";
    private static final String KR_ENDPOINT   = "https://kr.api.riotgames.com";

    private static final String ACCOUNT_URL      = ASIA_ENDPOINT + "/riot/account/v1/accounts/by-riot-id/%s/%s";
    private static final String MATCH_IDS_URL    = ASIA_ENDPOINT + "/lol/match/v5/matches/by-puuid/%s/ids?startTime=%d&endTime=%d&start=%d&count=100";
    private static final String MATCH_DETAIL_URL = ASIA_ENDPOINT + "/lol/match/v5/matches/%s";
    private static final String SUMMONER_URL     = KR_ENDPOINT + "/lol/summoner/v4/summoners/by-puuid/%s";
    private static final String LEAGUE_URL       = KR_ENDPOINT + "/lol/league/v4/entries/by-puuid/%s";

    // Redis TTL
    private static final Duration PUUID_TTL   = Duration.ofHours(24);
    private static final Duration SUMMONER_TTL = Duration.ofHours(1);
    private static final Duration LEAGUE_TTL  = Duration.ofMinutes(5);

    @Value("${riot.api.key}")
    private String apiKey;

    public RiotFetcherImpl(RestTemplate restTemplate,
                            StringRedisTemplate redis,
                            MatchJpaRepository matchRepo,
                            MatchParticipantJpaRepository participantRepo,
                            ObjectMapper objectMapper) {
        this.restTemplate   = restTemplate;
        this.redis          = redis;
        this.matchRepo      = matchRepo;
        this.participantRepo = participantRepo;
        this.objectMapper   = objectMapper;
    }

    // ── PUUID ──────────────────────────────────────────────────────────────

    @Override
    public String fetchPuuid(String gameName, String tagLine) {
        String key = "puuid:" + gameName + ":" + tagLine;

        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) return cached;
        } catch (Exception e) {
            log.warn("[Redis] puuid 조회 실패, API fallback: {}", e.getMessage());
        }

        String url = String.format(ACCOUNT_URL, gameName, tagLine);
        Map response = get(url, Map.class);

        if (response == null || response.get("puuid") == null) {
            throw new RuntimeException("Failed to fetch puuid for " + gameName + "#" + tagLine);
        }

        String puuid = response.get("puuid").toString();

        try {
            redis.opsForValue().set(key, puuid, PUUID_TTL);
        } catch (Exception e) {
            log.warn("[Redis] puuid 저장 실패: {}", e.getMessage());
        }

        return puuid;
    }

    // ── SummonerInfo ───────────────────────────────────────────────────────

    @Override
    public SummonerInfo fetchSummoner(String puuid) {
        String key = "summoner:" + puuid;

        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                Map map = objectMapper.readValue(cached, Map.class);
                return new SummonerInfo(
                        ((Number) map.get("profileIconId")).intValue(),
                        ((Number) map.get("summonerLevel")).longValue()
                );
            }
        } catch (Exception e) {
            log.warn("[Redis] summoner 조회 실패, API fallback: {}", e.getMessage());
        }

        String url = String.format(SUMMONER_URL, puuid);
        Map response = get(url, Map.class);

        if (response == null || response.get("profileIconId") == null) {
            throw new RuntimeException("Failed to fetch summoner for puuid=" + puuid);
        }

        int profileIconId  = ((Number) response.get("profileIconId")).intValue();
        long summonerLevel = ((Number) response.get("summonerLevel")).longValue();

        try {
            String json = objectMapper.writeValueAsString(
                    Map.of("profileIconId", profileIconId, "summonerLevel", summonerLevel));
            redis.opsForValue().set(key, json, SUMMONER_TTL);
        } catch (Exception e) {
            log.warn("[Redis] summoner 저장 실패: {}", e.getMessage());
        }

        return new SummonerInfo(profileIconId, summonerLevel);
    }

    // ── LeagueEntries ──────────────────────────────────────────────────────

    @Override
    public List<LeagueEntry> fetchLeagueEntries(String puuid) {
        String key = "league:" + puuid;

        try {
            String cached = redis.opsForValue().get(key);
            if (cached != null) {
                List<Map> list = objectMapper.readValue(cached, List.class);
                return parseLeagueEntries(list);
            }
        } catch (Exception e) {
            log.warn("[Redis] league 조회 실패, API fallback: {}", e.getMessage());
        }

        String url = String.format(LEAGUE_URL, puuid);
        List<Map> response = get(url, List.class);
        if (response == null) return List.of();

        List<LeagueEntry> entries = parseLeagueEntries(response);

        try {
            redis.opsForValue().set(key, objectMapper.writeValueAsString(response), LEAGUE_TTL);
        } catch (Exception e) {
            log.warn("[Redis] league 저장 실패: {}", e.getMessage());
        }

        return entries;
    }

    private List<LeagueEntry> parseLeagueEntries(List<Map> list) {
        List<LeagueEntry> entries = new ArrayList<>();
        for (Map entry : list) {
            String queueTypeStr = (String) entry.get("queueType");
            QueueType queueType = switch (queueTypeStr) {
                case "RANKED_SOLO_5x5" -> QueueType.SOLO;
                case "RANKED_FLEX_SR"  -> QueueType.FLEX;
                default -> null;
            };
            if (queueType == null) continue;

            String tier = (String) entry.get("tier");
            String rank = (String) entry.get("rank");
            int lp      = ((Number) entry.get("leaguePoints")).intValue();
            int wins    = ((Number) entry.get("wins")).intValue();
            int losses  = ((Number) entry.get("losses")).intValue();

            entries.add(new LeagueEntry(queueType, tier, rank, lp, wins, losses));
        }
        return entries;
    }

    // ── Matches ────────────────────────────────────────────────────────────

    private static final int MATCH_PAGE_SIZE = 100;

    @Override
    public List<Match> fetchMatches(String puuid, long start, long end) {
        // 1. DB에서 해당 기간 참가자 레코드 조회
        List<MatchParticipantEntity> cached =
                participantRepo.findByPuuidAndGameStartBetween(puuid, start, end);

        // 2. Riot API에서 matchId 목록 조회
        List<String> allMatchIds = fetchAllMatchIds(puuid, start, end);

        if (allMatchIds.isEmpty() && cached.isEmpty()) return List.of();

        // 3. DB에 없는 matchId만 API 호출
        java.util.Set<String> cachedMatchIds = cached.stream()
                .map(MatchParticipantEntity::getMatchId)
                .collect(java.util.stream.Collectors.toSet());

        List<String> missingIds = allMatchIds.stream()
                .filter(id -> !cachedMatchIds.contains(id))
                .toList();

        log.info("[RiotAPI] puuid={} | 전체 matchIds: {}건, DB 캐시: {}건, API 호출: {}건",
                puuid.substring(0, 8), allMatchIds.size(), cached.size(), missingIds.size());

        // 4. 누락된 매치 API 호출 후 DB 저장
        List<Match> freshMatches = new ArrayList<>();
        for (String matchId : missingIds) {
            Map matchResponse = get(String.format(MATCH_DETAIL_URL, matchId), Map.class);
            if (matchResponse == null) continue;

            saveMatchToDb(matchId, matchResponse);

            Match match = extractParticipantMatch(matchResponse, puuid);
            if (match != null) freshMatches.add(match);
        }

        // 5. DB 캐시 + 신규 결과 합산
        List<Match> result = new ArrayList<>();
        cached.stream().map(MatchParticipantEntity::toDomain).forEach(result::add);
        result.addAll(freshMatches);
        return result;
    }

    private void saveMatchToDb(String matchId, Map matchResponse) {
        try {
            Map info = (Map) matchResponse.get("info");
            if (info == null) return;

            int queueId    = ((Number) info.get("queueId")).intValue();
            long gameStart = ((Number) info.get("gameStartTimestamp")).longValue();
            String rawJson = objectMapper.writeValueAsString(matchResponse);

            // matches 테이블 저장 (중복이면 skip)
            if (!matchRepo.existsById(matchId)) {
                matchRepo.save(new MatchEntity(matchId, queueId, gameStart, rawJson));
            }

            // match_participants 저장
            List participants = (List) info.get("participants");
            List<MatchParticipantEntity> toSave = new ArrayList<>();
            for (Object obj : participants) {
                Map p = (Map) obj;
                String pPuuid = (String) p.get("puuid");
                MatchParticipantId pid = new MatchParticipantId(matchId, pPuuid);
                if (participantRepo.existsById(pid)) continue;

                toSave.add(new MatchParticipantEntity(
                        matchId, pPuuid,
                        (boolean) p.get("win"),
                        ((Number) p.get("kills")).intValue(),
                        ((Number) p.get("deaths")).intValue(),
                        ((Number) p.get("assists")).intValue(),
                        ((Number) p.get("championId")).intValue(),
                        (String) p.get("championName"),
                        queueId, gameStart
                ));
            }
            participantRepo.saveAll(toSave);

        } catch (JsonProcessingException e) {
            log.warn("[DB] matchId={} 저장 실패: {}", matchId, e.getMessage());
        }
    }

    private Match extractParticipantMatch(Map matchResponse, String puuid) {
        Map info = (Map) matchResponse.get("info");
        if (info == null) return null;

        int queueId = ((Number) info.get("queueId")).intValue();
        QueueType queueType = QueueType.from(queueId);

        List participants = (List) info.get("participants");
        for (Object obj : participants) {
            Map p = (Map) obj;
            if (puuid.equals(p.get("puuid"))) {
                return new Match(
                        (boolean) p.get("win"),
                        ((Number) p.get("kills")).intValue(),
                        ((Number) p.get("deaths")).intValue(),
                        ((Number) p.get("assists")).intValue(),
                        queueType,
                        ((Number) p.get("championId")).intValue(),
                        (String) p.get("championName")
                );
            }
        }
        return null;
    }

    private List<String> fetchAllMatchIds(String puuid, long start, long end) {
        List<String> allIds = new ArrayList<>();
        int offset = 0;

        while (true) {
            String url = String.format(MATCH_IDS_URL, puuid, start / 1000, end / 1000, offset);
            List<String> page = get(url, List.class);

            if (page == null || page.isEmpty()) break;

            allIds.addAll(page);

            if (page.size() < MATCH_PAGE_SIZE) break;
            offset += MATCH_PAGE_SIZE;
        }

        return allIds;
    }

    // ── HTTP helper ────────────────────────────────────────────────────────

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
}
