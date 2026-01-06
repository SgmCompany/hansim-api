package com.sgm.hansimapi.domain.player.dto;

import com.sgm.hansimapi.domain.player.entity.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Player 생성/수정 요청")
public class PlayerRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "플레이어 이름", example = "홍길동")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Schema(description = "플레이어 이메일", example = "player@example.com")
    private String email;

    @Schema(description = "포지션", example = "FW")
    private String position;

    @Positive(message = "등번호는 양수여야 합니다.")
    @Schema(description = "등번호", example = "10")
    private Integer jerseyNumber;

    @Positive(message = "나이는 양수여야 합니다.")
    @Schema(description = "나이", example = "25")
    private Integer age;

    public Player toEntity() {
        return Player.builder()
                .name(name)
                .email(email)
                .position(position)
                .jerseyNumber(jerseyNumber)
                .age(age)
                .build();
    }
}