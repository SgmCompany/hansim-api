package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.WorkType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "내 프로필 응답")
public class MyProfileResponse {

    @Schema(description = "유저 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private final Long id;

    @Schema(description = "이메일", example = "user@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private final String email;

    @Schema(description = "근무 유형. 미등록 시 null", example = "NINE_TO_SIX", nullable = true)
    private final WorkType workType;

    @Schema(description = "급여 (원 단위). NINE_TO_SIX/FAIR_24H=연봉, PART_TIME=시급. STUDENT/UNEMPLOYED는 null", example = "50000000", nullable = true)
    private final Integer salaryAmount;

    @Schema(description = "연동된 소환사 정보. 미연동 시 null", nullable = true)
    private final SummonerInfo summoner;

    private MyProfileResponse(Long id, String email, WorkType workType,
                              Integer salaryAmount, SummonerInfo summoner) {
        this.id           = id;
        this.email        = email;
        this.workType     = workType;
        this.salaryAmount = salaryAmount;
        this.summoner     = summoner;
    }

    public static MyProfileResponse from(User user) {
        SummonerInfo summoner = user.hasSummoner()
                ? new SummonerInfo(user.getRiotGameName(), user.getRiotTagLine())
                : null;
        return new MyProfileResponse(user.getId(), user.getEmail(),
                user.getWorkType(), user.getSalaryAmount(), summoner);
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
