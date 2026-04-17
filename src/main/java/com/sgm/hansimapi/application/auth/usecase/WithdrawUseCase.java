package com.sgm.hansimapi.application.auth.usecase;

import com.sgm.hansimapi.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WithdrawUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(Long userId) {
        userRepository.findById(userId)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new IllegalStateException("이미 탈퇴한 회원이거나 존재하지 않는 회원입니다."));

        userRepository.softDelete(userId);
    }
}
