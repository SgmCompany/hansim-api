package com.sgm.hansimapi.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "구글 소셜 로그인 요청")
public record GoogleLoginRequestBody(

        @Schema(
                description = "프론트엔드 Google Sign-In SDK에서 발급받은 ID token (JWT 형식, eyJ로 시작)",
                type = "string",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String googleToken
) {}
