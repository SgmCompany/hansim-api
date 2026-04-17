package com.sgm.hansimapi.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 토큰 응답")
public record AuthResponse(

        @Schema(
                description = "JWT access token. 유효기간 7일",
                type = "string",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String accessToken
) {
    public static AuthResponse from(String accessToken) {
        return new AuthResponse(accessToken);
    }
}
