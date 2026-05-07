package com.sgm.hansimapi.application.user.usecase;

import com.sgm.hansimapi.domain.user.UserRepository;
import com.sgm.hansimapi.domain.user.WorkType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateProfileUseCase {

    private final UserRepository userRepository;

    @Transactional
    public void execute(Long userId, WorkType workType, Integer salaryAmount) {
        // 소득 미수집 유형(STUDENT, UNEMPLOYED)은 급여 정보를 저장하지 않음
        Integer savedSalary = workType.collectsIncome() ? salaryAmount : null;
        userRepository.updateProfile(userId, workType, savedSalary);
    }
}
