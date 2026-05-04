package com.sgm.hansimapi.application.user.usecase;

import com.sgm.hansimapi.application.user.SummonerPrefetchService;
import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.sgm.hansimapi.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkSummonerUseCase {

    private final UserRepository userRepository;
    private final RiotFetcher riotFetcher;
    private final SummonerPrefetchService prefetchService;

    /**
     * Riot ID 실존 여부를 검증한 뒤 puuid와 함께 유저에 소환사 정보를 연동한다.
     * 연동 완료 후 최근 7일 매치 데이터를 백그라운드로 프리페치한다.
     */
    @Transactional
    public void execute(Long userId, String riotGameName, String riotTagLine) {
        // Riot API 검증 + puuid 획득
        String puuid = riotFetcher.fetchPuuid(riotGameName, riotTagLine);

        userRepository.updateSummoner(userId, riotGameName, riotTagLine, puuid);

        // 트랜잭션 커밋 후 비동기 실행되므로 사용자 응답에 영향 없음
        prefetchService.prefetchRecentMatches(puuid);
    }
}
