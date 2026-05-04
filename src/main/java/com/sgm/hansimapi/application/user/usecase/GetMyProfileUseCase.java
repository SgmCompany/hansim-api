package com.sgm.hansimapi.application.user.usecase;

import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMyProfileUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User execute(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
    }
}
