package com.sgm.hansimapi.application.user.usecase;

import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnlinkSummonerUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (!user.hasSummoner()) {
            throw new IllegalStateException("연동된 소환사가 없습니다.");
        }

        userRepository.updateSummoner(userId, null, null, null);
    }
}
