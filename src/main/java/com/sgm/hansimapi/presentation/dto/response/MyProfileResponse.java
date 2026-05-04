package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "내 프로필 응답")
public class MyProfileResponse {

    @Schema(description = "유저 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Long id;

    @Schema(description = "이메일", example = "user@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String email;

    @Schema(description = "연동된 소환사 정보. 미연동 시 null", nullable = true)
    private final SummonerInfo summoner;

    private MyProfileResponse(Long id, String email, SummonerInfo summoner) {
        this.id       = id;
        this.email    = email;
        this.summoner = summoner;
    }

    public static MyProfileResponse from(User user) {
        SummonerInfo summoner = user.hasSummoner()
                ? new SummonerInfo(user.getRiotGameName(), user.getRiotTagLine())
                : null;
        return new MyProfileResponse(user.getId(), user.getEmail(), summoner);
    }

    @Getter
    @Schema(description = "연동된 소환사 정보")
    public static class SummonerInfo {

        @Schema(description = "Riot 게임 이름", example = "페이커", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String gameName;

        @Schema(description = "Riot 태그라인", example = "KR1", requiredMode = Schema.RequiredMode.REQUIRED)
        private final String tagLine;

        public SummonerInfo(String gameName, String tagLine) {
            this.gameName = gameName;
            this.tagLine  = tagLine;
        }
    }
}
