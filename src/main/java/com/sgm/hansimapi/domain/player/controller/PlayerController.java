package com.sgm.hansimapi.domain.player.controller;

import com.sgm.hansimapi.domain.player.dto.PlayerRequest;
import com.sgm.hansimapi.domain.player.dto.PlayerResponse;
import com.sgm.hansimapi.domain.player.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Tag(name = "Player", description = "플레이어 관리 API")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/test")
    @Operation(summary = "테스트 API", description = "API 동작 확인을 위한 테스트 엔드포인트입니다.")
    public ResponseEntity<String> test() {
        log.info("Test API called");
        return ResponseEntity.ok("Test API is working!");
    }

    @GetMapping
    @Operation(summary = "모든 플레이어 조회", description = "등록된 모든 플레이어 목록을 조회합니다.")
    public ResponseEntity<List<PlayerResponse>> getAllPlayers() {
        List<PlayerResponse> players = playerService.getAllPlayers();
        return ResponseEntity.ok(players);
    }

    @GetMapping("/{id}")
    @Operation(summary = "플레이어 단건 조회", description = "ID로 특정 플레이어를 조회합니다.")
    public ResponseEntity<PlayerResponse> getPlayerById(
            @Parameter(description = "플레이어 ID", required = true)
            @PathVariable Long id) {
        PlayerResponse player = playerService.getPlayerById(id);
        return ResponseEntity.ok(player);
    }

    @PostMapping
    @Operation(summary = "플레이어 생성", description = "새로운 플레이어를 생성합니다.")
    public ResponseEntity<PlayerResponse> createPlayer(
            @Valid @RequestBody PlayerRequest request) {
        PlayerResponse createdPlayer = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @PutMapping("/{id}")
    @Operation(summary = "플레이어 수정", description = "기존 플레이어 정보를 수정합니다.")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @Parameter(description = "플레이어 ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request) {
        PlayerResponse updatedPlayer = playerService.updatePlayer(id, request);
        return ResponseEntity.ok(updatedPlayer);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "플레이어 삭제", description = "플레이어를 삭제합니다.")
    public ResponseEntity<Void> deletePlayer(
            @Parameter(description = "플레이어 ID", required = true)
            @PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}