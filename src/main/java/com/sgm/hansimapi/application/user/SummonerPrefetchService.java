package com.sgm.hansimapi.application.user;

import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.sgm.hansimapi.domain.summary.TimeWindow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SummonerPrefetchService {

    private static final int PREFETCH_DAYS = 7;

    private final RiotFetcher riotFetcher;

    /**
     * 소환사 연동 완료 후 최근 7일 매치 데이터를 백그라운드로 프리페치한다.
     * RiotFetcherImpl.fetchMatches() 내부에서 DB 캐시로 저장되므로
     * 이후 실제 조회 시 Riot API 호출 없이 즉시 응답 가능하다.
     */
    @Async("prefetchExecutor")
    public void prefetchRecentMatches(String puuid) {
        try {
            TimeWindow window = TimeWindow.ofDays(PREFETCH_DAYS);
            log.info("[prefetch] 소환사 {} 최근 {}일 데이터 수집 시작", puuid, PREFETCH_DAYS);
            riotFetcher.fetchMatches(puuid, window.getStart(), window.getEnd());
            log.info("[prefetch] 소환사 {} 데이터 수집 완료", puuid);
        } catch (Exception e) {
            // 프리페치 실패는 사용자 경험에 영향 없음 — 이후 실시간 조회로 대체됨
            log.warn("[prefetch] 소환사 {} 데이터 수집 실패: {}", puuid, e.getMessage());
        }
    }
}
