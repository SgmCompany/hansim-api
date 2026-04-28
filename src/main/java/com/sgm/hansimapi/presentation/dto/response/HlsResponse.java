package com.sgm.hansimapi.presentation.dto.response;

import com.sgm.hansimapi.domain.summary.HlsResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/** 한심지수(HLS) 응답 DTO — SummaryResponse, BatchSummaryResponse 공용 */
@Getter
@Schema(description = "한심지수 (NINE_TO_SIX 기준)")
public class HlsResponse {

    @Schema(description = "최종 HLS (0~100)", example = "72")
    private final int total;

    @Schema(description = "부분 점수 상세")
    private final HlsDetail detail;

    private HlsResponse(int total, HlsDetail detail) {
        this.total  = total;
        this.detail = detail;
    }

    public static HlsResponse from(HlsResult hls) {
        return new HlsResponse(
                hls.getTotal(),
                new HlsDetail(hls.getVolume(), hls.getResult(), hls.getLateNight(),
                        hls.getWeekend(), hls.getSession(), hls.getLosingStreak(), hls.getTilt())
        );
    }

    @Getter
    @Schema(description = "HLS 부분 점수")
    public static class HlsDetail {

        @Schema(description = "A. 볼륨 (0~30): 총 플레이 분 + 판수", example = "24")
        private final int volume;

        @Schema(description = "B. 결과 (0~20): 패배/서렌/KDA 기반", example = "16")
        private final int result;

        @Schema(description = "C. 심야 (0~15): KST 00~06시 누적 분", example = "10")
        private final int lateNight;

        @Schema(description = "D. 주말 (0~10): 토/일 누적 분", example = "0")
        private final int weekend;

        @Schema(description = "E. 세션 (0~10): 최장 연속 플레이", example = "8")
        private final int session;

        @Schema(description = "F. 연패 (0~10): 최대 연속 패배", example = "10")
        private final int losingStreak;

        @Schema(description = "G. 분노재큐 (0~5): 패배 후 5분 내 재입장", example = "4")
        private final int tilt;

        HlsDetail(int volume, int result, int lateNight, int weekend,
                  int session, int losingStreak, int tilt) {
            this.volume       = volume;
            this.result       = result;
            this.lateNight    = lateNight;
            this.weekend      = weekend;
            this.session      = session;
            this.losingStreak = losingStreak;
            this.tilt         = tilt;
        }
    }
}
