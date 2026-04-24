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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    // [5] 429 공유 백오프: 여러 스레드가 동시에 429를 받을 때 중복 sleep 방지
    // 값 = 백오프 해제 시각 (System.currentTimeMillis 기준 ms)
    // 한 스레드가 설정하면 다른 스레드는 해당 시각까지 대기 후 진행
    private final AtomicLong sharedBackoffUntilMs = new AtomicLong(0);

    private static final String ASIA_ENDPOINT = "https://asia.api.riotgames.com";
    private static final String KR_ENDPOINT   = "https://kr.api.riotgames.com";

    private static final String ACCOUNT_URL      = ASIA_ENDPOINT + "/riot/account/v1/accounts/by-riot-id/%s/%s";
    private static final String MATCH_IDS_URL    = ASIA_ENDPOINT + "/lol/match/v5/matches/by-puuid/%s/ids?startTime=%d&endTime=%d&start=%d&count=100";
    private static final String MATCH_DETAIL_URL = ASIA_ENDPOINT + "/lol/match/v5/matches/%s";
    private static final String SUMMONER_URL     = KR_ENDPOINT + "/lol/summoner/v4/summoners/by-puuid/%s";
    private static final String LEAGUE_URL       = KR_ENDPOINT + "/lol/league/v4/entries/by-puuid/%s";

    // Redis TTL
    private static final Duration PUUID_TTL      = Duration.ofHours(24);
    private static final Duration SUMMONER_TTL   = Duration.ofHours(1);
    private static final Duration LEAGUE_TTL     = Duration.ofMinutes(5);
    // [6] matchId 목록: 짧은 TTL로 캐싱하여 동일 사용자 재조회 시 API 절감
    private static final Duration MATCH_IDS_TTL  = Duration.ofMinutes(3);

    @Value("${riot.api.key}")
    private String apiKey;

    public RiotFetcherImpl(RestTemplate restTemplate,
                            StringRedisTemplate redis,
                            MatchJpaRepository matchRepo,
                            MatchParticipantJpaRepository participantRepo,
                            ObjectMapper objectMapper) {
        this.restTemplate    = restTemplate;
        this.redis           = redis;
        this.matchRepo       = matchRepo;
        this.participantRepo = participantRepo;
        this.objectMapper    = objectMapper;
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
        List<MatchParticipantEntity> cachedEntities =
                participantRepo.findByPuuidAndGameStartBetween(puuid, start, end);

        // 2. [6] Redis에서 matchId 목록 조회, miss 시 Riot API 호출
        List<String> allMatchIds = fetchAllMatchIds(puuid, start, end);

        if (allMatchIds.isEmpty() && cachedEntities.isEmpty()) return List.of();

        // 3. DB에 없는 matchId만 API 호출
        Set<String> cachedMatchIds = cachedEntities.stream()
                .map(MatchParticipantEntity::getMatchId)
                .collect(Collectors.toSet());

        List<String> missingIds = allMatchIds.stream()
                .filter(id -> !cachedMatchIds.contains(id))
                .toList();

        log.info("[RiotAPI] puuid={} | 전체 matchIds: {}건, DB 캐시: {}건, API 호출: {}건",
                puuid.substring(0, 8), allMatchIds.size(), cachedEntities.size(), missingIds.size());

        // 4. 누락된 매치 API 호출 후 DB 저장
        List<Match> freshMatches = new ArrayList<>();
        for (String matchId : missingIds) {
            Map matchResponse = get(String.format(MATCH_DETAIL_URL, matchId), Map.class);
            if (matchResponse == null) continue;

            saveMatchToDb(matchId, matchResponse);

            Match match = extractParticipantMatch(matchResponse, puuid);
            if (match != null) freshMatches.add(match);
        }

        // 5. DB 캐시 + 신규 결과 합산 후 최신순 정렬 (Streak 계산 기준)
        List<Match> result = new ArrayList<>();
        cachedEntities.stream().map(MatchParticipantEntity::toDomain).forEach(result::add);
        result.addAll(freshMatches);
        result.sort(Comparator.comparingLong(Match::getGameStart).reversed());
        return result;
    }

    // [1] 트랜잭션: matches + match_participants를 하나의 트랜잭션으로 묶어 일관성 보장
    // [2] N+1 개선: existsById 개별 호출 제거 → 배치 조회 후 필터링
    @Transactional
    protected void saveMatchToDb(String matchId, Map matchResponse) {
        try {
            Map info = (Map) matchResponse.get("info");
            if (info == null) return;

            int queueId      = ((Number) info.get("queueId")).intValue();
            long gameStart   = ((Number) info.get("gameStartTimestamp")).longValue();
            int gameDuration = ((Number) info.get("gameDuration")).intValue();
            String rawJson   = objectMapper.writeValueAsString(matchResponse);

            // matches 테이블 저장 (중복이면 skip)
            if (!matchRepo.existsById(matchId)) {
                matchRepo.save(new MatchEntity(matchId, queueId, gameStart, rawJson));
            }

            // [2] 10명 각각 existsById 대신 배치 조회 → 1 SELECT로 기존 레코드 확인
            List participants = (List) info.get("participants");
            List<MatchParticipantId> allPids = new ArrayList<>();
            for (Object obj : participants) {
                Map p = (Map) obj;
                allPids.add(new MatchParticipantId(matchId, (String) p.get("puuid")));
            }
            Set<MatchParticipantId> existingPids = participantRepo.findAllById(allPids)
                    .stream()
                    .map(e -> new MatchParticipantId(e.getMatchId(), e.getPuuid()))
                    .collect(Collectors.toSet());

            List<MatchParticipantEntity> toSave = new ArrayList<>();
            for (Object obj : participants) {
                Map p = (Map) obj;
                String pPuuid = (String) p.get("puuid");
                if (existingPids.contains(new MatchParticipantId(matchId, pPuuid))) continue;

                toSave.add(new MatchParticipantEntity(
                        matchId, pPuuid,
                        (boolean) p.get("win"),
                        ((Number) p.get("kills")).intValue(),
                        ((Number) p.get("deaths")).intValue(),
                        ((Number) p.get("assists")).intValue(),
                        ((Number) p.get("championId")).intValue(),
                        (String) p.get("championName"),
                        queueId, gameStart,
                        (String) p.get("teamPosition"),
                        ((Number) p.get("totalMinionsKilled")).intValue(),
                        ((Number) p.get("neutralMinionsKilled")).intValue(),
                        gameDuration,
                        ((Number) p.get("totalDamageDealtToChampions")).intValue(),
                        ((Number) p.get("visionScore")).intValue(),
                        ((Number) p.get("wardsPlaced")).intValue(),
                        ((Number) p.get("wardsKilled")).intValue(),
                        ((Number) p.get("doubleKills")).intValue(),
                        ((Number) p.get("tripleKills")).intValue(),
                        ((Number) p.get("quadraKills")).intValue(),
                        ((Number) p.get("pentaKills")).intValue()
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

        int queueId      = ((Number) info.get("queueId")).intValue();
        int gameDuration = ((Number) info.get("gameDuration")).intValue();
        long gameStart   = ((Number) info.get("gameStartTimestamp")).longValue();
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
                        (String) p.get("championName"),
                        (String) p.get("teamPosition"),
                        ((Number) p.get("totalMinionsKilled")).intValue(),
                        ((Number) p.get("neutralMinionsKilled")).intValue(),
                        gameDuration,
                        ((Number) p.get("totalDamageDealtToChampions")).intValue(),
                        ((Number) p.get("visionScore")).intValue(),
                        ((Number) p.get("wardsPlaced")).intValue(),
                        ((Number) p.get("wardsKilled")).intValue(),
                        ((Number) p.get("doubleKills")).intValue(),
                        ((Number) p.get("tripleKills")).intValue(),
                        ((Number) p.get("quadraKills")).intValue(),
                        ((Number) p.get("pentaKills")).intValue(),
                        gameStart
                );
            }
        }
        return null;
    }

    // [6] matchId 목록 Redis 캐싱 (TTL: 3분)
    // 동일 사용자 재조회 시 matchId 목록 API 호출을 건너뜀
    private List<String> fetchAllMatchIds(String puuid, long start, long end) {
        String cacheKey = String.format("matchids:%s:%d:%d", puuid, start / 1000, end / 1000);

        try {
            String cached = redis.opsForValue().get(cacheKey);
            if (cached != null) {
                List<String> ids = objectMapper.readValue(cached, List.class);
                log.debug("[Redis] matchIds 캐시 hit: puuid={}, {}건", puuid.substring(0, 8), ids.size());
                return ids;
            }
        } catch (Exception e) {
            log.warn("[Redis] matchIds 조회 실패, API fallback: {}", e.getMessage());
        }

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

        try {
            redis.opsForValue().set(cacheKey, objectMapper.writeValueAsString(allIds), MATCH_IDS_TTL);
        } catch (Exception e) {
            log.warn("[Redis] matchIds 저장 실패: {}", e.getMessage());
        }

        return allIds;
    }

    // ── HTTP helper ────────────────────────────────────────────────────────

    private static final int MAX_RETRIES = 2;

    private <T> T get(String url, Class<T> responseType) {
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            // [5] 공유 백오프: 다른 스레드가 설정한 backoff 시각까지 대기
            waitForSharedBackoff();

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

                // [5] 공유 백오프 시각 설정: 모든 스레드가 이 시각 이후에 재시도
                long backoffUntil = System.currentTimeMillis() + waitSec * 1000;
                sharedBackoffUntilMs.updateAndGet(prev -> Math.max(prev, backoffUntil));

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

    private void waitForSharedBackoff() {
        long backoffUntil = sharedBackoffUntilMs.get();
        long remaining = backoffUntil - System.currentTimeMillis();
        if (remaining <= 0) return;

        log.debug("[RiotAPI] 공유 백오프 대기 중: {}ms", remaining);
        try {
            TimeUnit.MILLISECONDS.sleep(remaining);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
