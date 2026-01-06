package com.sgm.hansimapi.domain.player.dto;

import com.sgm.hansimapi.domain.player.entity.Player;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Player 응답")
public class PlayerResponse {

    @Schema(description = "플레이어 ID", example = "1")
    private Long id;

    @Schema(description = "플레이어 이름", example = "홍길동")
    private String name;

    @Schema(description = "플레이어 이메일", example = "player@example.com")
    private String email;

    @Schema(description = "포지션", example = "FW")
    private String position;

    @Schema(description = "등번호", example = "10")
    private Integer jerseyNumber;

    @Schema(description = "나이", example = "25")
    private Integer age;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    public static PlayerResponse from(Player player) {
        return PlayerResponse.builder()
                .id(player.getId())
                .name(player.getName())
                .email(player.getEmail())
                .position(player.getPosition())
                .jerseyNumber(player.getJerseyNumber())
                .age(player.getAge())
                .createdAt(player.getCreatedAt())
                .updatedAt(player.getUpdatedAt())
                .build();
    }
}