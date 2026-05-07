package com.sgm.hansimapi.application.summary.usecase;

import com.sgm.hansimapi.application.summary.command.BatchSummaryCommand;
import com.sgm.hansimapi.domain.riot.LeagueEntry;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.RiotId;
import com.sgm.hansimapi.domain.riot.SummonerInfo;
import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.sgm.hansimapi.domain.summary.BatchPlayerSummary;
import com.sgm.hansimapi.domain.summary.BatchSummary;
import com.sgm.hansimapi.domain.summary.TimeWindow;
import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.UserRepository;
import com.sgm.hansimapi.domain.user.WorkType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class BatchSummaryUseCase {

    private final RiotFetcher riotFetcher;
    private final UserRepository userRepository;
    private final Executor batchExecutor;

    public BatchSummaryUseCase(RiotFetcher riotFetcher,
                                UserRepository userRepository,
                                @Qualifier("batchExecutor") Executor batchExecutor) {
        this.riotFetcher = riotFetcher;
        this.userRepository = userRepository;
        this.batchExecutor = batchExecutor;
    }

    /**
     * @param userId 로그인 유저 ID. 비로그인 시 null — 최저시급으로 economicImpact 계산
     */
    public BatchSummary execute(BatchSummaryCommand command, Long userId) {
        WorkType workType = null;
        Integer salaryAmount = null;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.hasProfile()) {
                workType     = user.getWorkType();
                salaryAmount = user.getSalaryAmount();
            }
        }

        final WorkType callerWorkType = workType;
        final Integer callerSalary   = salaryAmount;

        // 소환사별 Riot API 호출을 병렬로 실행
        List<CompletableFuture<BatchPlayerSummary>> futures = command.getRiotIds().stream()
                .map(riotId -> CompletableFuture.supplyAsync(
                        () -> fetchPlayer(riotId, command.getTimeWindow(), callerWorkType, callerSalary),
                        batchExecutor
                ))
                .toList();

        // 전체 완료 대기 후 순서 유지하여 수집 (하나 실패 시 나머지 취소)
        List<BatchPlayerSummary> players;
        try {
            players = futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } catch (Exception e) {
            futures.forEach(f -> f.cancel(true));
            throw e;
        }

        return new BatchSummary(command.getTimeWindow(), players);
    }

    private BatchPlayerSummary fetchPlayer(RiotId riotId, TimeWindow timeWindow,
                                           WorkType callerWorkType, Integer callerSalaryAmount) {
        String puuid = riotFetcher.fetchPuuid(riotId.getGameName(), riotId.getTagLine());
        SummonerInfo summonerInfo = riotFetcher.fetchSummoner(puuid);
        List<LeagueEntry> leagueEntries = riotFetcher.fetchLeagueEntries(puuid);
        List<Match> matches = riotFetcher.fetchMatches(puuid, timeWindow.getStart(), timeWindow.getEnd());

        String riotIdStr = riotId.getGameName() + "#" + riotId.getTagLine();
        return BatchPlayerSummary.from(riotIdStr, summonerInfo, leagueEntries, matches,
                callerWorkType, callerSalaryAmount);
    }
}
