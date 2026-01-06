package com.sgm.hansimapi.domain.player.service;

import com.sgm.hansimapi.domain.player.dto.PlayerRequest;
import com.sgm.hansimapi.domain.player.dto.PlayerResponse;
import com.sgm.hansimapi.domain.player.entity.Player;
import com.sgm.hansimapi.domain.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlayerService {

    private final PlayerRepository playerRepository;

    /**
     * 모든 플레이어 조회
     */
    public List<PlayerResponse> getAllPlayers() {
        return playerRepository.findAll().stream()
                .map(PlayerResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 플레이어 ID로 조회
     */
    public PlayerResponse getPlayerById(Long id) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("플레이어를 찾을 수 없습니다. ID: " + id));
        return PlayerResponse.from(player);
    }

    /**
     * 플레이어 생성
     */
    @Transactional
    public PlayerResponse createPlayer(PlayerRequest request) {
        // 이메일 중복 체크
        if (playerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }

        Player player = request.toEntity();
        Player savedPlayer = playerRepository.save(player);
        log.info("플레이어 생성 완료. ID: {}, Name: {}", savedPlayer.getId(), savedPlayer.getName());

        return PlayerResponse.from(savedPlayer);
    }

    /**
     * 플레이어 수정
     */
    @Transactional
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("플레이어를 찾을 수 없습니다. ID: " + id));

        // 이메일 변경 시 중복 체크
        if (!player.getEmail().equals(request.getEmail()) &&
            playerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + request.getEmail());
        }

        player.update(
                request.getName(),
                request.getEmail(),
                request.getPosition(),
                request.getJerseyNumber(),
                request.getAge()
        );

        log.info("플레이어 수정 완료. ID: {}, Name: {}", player.getId(), player.getName());

        return PlayerResponse.from(player);
    }

    /**
     * 플레이어 삭제
     */
    @Transactional
    public void deletePlayer(Long id) {
        if (!playerRepository.existsById(id)) {
            throw new IllegalArgumentException("플레이어를 찾을 수 없습니다. ID: " + id);
        }

        playerRepository.deleteById(id);
        log.info("플레이어 삭제 완료. ID: {}", id);
    }
}