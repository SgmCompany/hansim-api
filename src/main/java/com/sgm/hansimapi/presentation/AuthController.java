package com.sgm.hansimapi.presentation;

import com.sgm.hansimapi.application.auth.command.GoogleLoginCommand;
import com.sgm.hansimapi.application.auth.usecase.GoogleLoginUseCase;
import com.sgm.hansimapi.application.auth.usecase.WithdrawUseCase;
import com.sgm.hansimapi.presentation.dto.request.GoogleLoginRequestBody;
import com.sgm.hansimapi.presentation.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "소셜 로그인 / 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final GoogleLoginUseCase googleLoginUseCase;
    private final WithdrawUseCase withdrawUseCase;

    @Operation(summary = "구글 소셜 로그인/회원가입",
               description = "Google access token을 검증하고 JWT를 발급합니다. 최초 요청 시 자동 회원가입됩니다.")
    @PostMapping("/google")
    public AuthResponse googleLogin(@Valid @RequestBody GoogleLoginRequestBody request) {
        GoogleLoginUseCase.TokenResult result =
                googleLoginUseCase.execute(GoogleLoginCommand.of(request.googleToken()));
        return AuthResponse.from(result.accessToken());
    }

    @Operation(summary = "회원탈퇴",
               description = "현재 로그인된 회원을 soft delete 처리합니다. 탈퇴 후 기존 토큰은 즉시 무효화됩니다.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/withdraw")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal Long userId) {
        withdrawUseCase.execute(userId);
    }
}
