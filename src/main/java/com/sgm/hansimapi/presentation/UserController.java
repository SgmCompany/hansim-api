package com.sgm.hansimapi.presentation;

import com.sgm.hansimapi.application.user.usecase.GetMyProfileUseCase;
import com.sgm.hansimapi.application.user.usecase.LinkSummonerUseCase;
import com.sgm.hansimapi.application.user.usecase.UnlinkSummonerUseCase;
import com.sgm.hansimapi.presentation.dto.request.LinkSummonerRequest;
import com.sgm.hansimapi.presentation.dto.response.MyProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "유저 프로필 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final LinkSummonerUseCase linkSummonerUseCase;
    private final UnlinkSummonerUseCase unlinkSummonerUseCase;

    @Operation(summary = "내 프로필 조회", description = "로그인한 유저의 프로필과 소환사 연동 정보를 반환합니다.")
    @GetMapping("/me")
    public MyProfileResponse getMyProfile(@AuthenticationPrincipal Long userId) {
        return MyProfileResponse.from(getMyProfileUseCase.execute(userId));
    }

    @Operation(summary = "소환사 연동/변경",
               description = "Riot ID를 검증한 뒤 내 계정에 연동합니다. 이미 연동된 경우 변경됩니다.")
    @PutMapping("/me/summoner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void linkSummoner(@AuthenticationPrincipal Long userId,
                             @Valid @RequestBody LinkSummonerRequest request) {
        linkSummonerUseCase.execute(userId, request.gameName(), request.tagLine());
    }

    @Operation(summary = "소환사 연동 해제", description = "연동된 소환사 정보를 삭제합니다.")
    @DeleteMapping("/me/summoner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlinkSummoner(@AuthenticationPrincipal Long userId) {
        unlinkSummonerUseCase.execute(userId);
    }
}
