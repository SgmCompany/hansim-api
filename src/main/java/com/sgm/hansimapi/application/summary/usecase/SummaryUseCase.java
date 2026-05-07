package com.sgm.hansimapi.application.summary.usecase;

import com.sgm.hansimapi.application.summary.command.SummaryCommand;
import com.sgm.hansimapi.domain.riot.Match;
import com.sgm.hansimapi.domain.riot.port.RiotFetcher;
import com.sgm.hansimapi.domain.summary.Summary;
import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.UserRepository;
import com.sgm.hansimapi.domain.user.WorkType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryUseCase {

    private final RiotFetcher riotFetcher;
    private final UserRepository userRepository;

    /**
     * @param command  조회 커맨드
     * @param userId   로그인 유저 ID. 비로그인 시 null — 최저시급으로 economicImpact 계산
     */
    public Summary execute(SummaryCommand command, Long userId) {
        String puuid = riotFetcher.fetchPuuid(command.getGameName(), command.getTagLine());

        List<Match> matches = riotFetcher.fetchMatches(
                puuid,
                command.getTimeWindow().getStart(),
                command.getTimeWindow().getEnd()
        );

        WorkType workType = null;
        Integer salaryAmount = null;
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.hasProfile()) {
                workType     = user.getWorkType();
                salaryAmount = user.getSalaryAmount();
            }
        }

        return Summary.from(matches, command, workType, salaryAmount);
    }
}
