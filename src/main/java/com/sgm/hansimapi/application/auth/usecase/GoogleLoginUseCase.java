package com.sgm.hansimapi.application.auth.usecase;

import com.sgm.hansimapi.application.auth.command.GoogleLoginCommand;
import com.sgm.hansimapi.config.JwtProvider;
import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.UserRepository;
import com.sgm.hansimapi.domain.user.port.GoogleAuthClient;
import com.sgm.hansimapi.domain.user.port.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoogleLoginUseCase {

    private final GoogleAuthClient googleAuthClient;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResult execute(GoogleLoginCommand command) {
        GoogleUserInfo userInfo = googleAuthClient.verify(command.getGoogleToken());

        User user = userRepository.findBySocialIdAndSocialType(userInfo.getSocialId(), userInfo.getSocialType())
                .orElseGet(() -> userRepository.save(
                        User.create(userInfo.getEmail(), userInfo.getSocialId(), userInfo.getSocialType())
                ));

        String accessToken = jwtProvider.generateAccessToken(user.getId(), user.getEmail());

        return new TokenResult(accessToken);
    }

    public record TokenResult(String accessToken) {}
}
