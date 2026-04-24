CREATE TABLE matches
(
    match_id   VARCHAR(20)  NOT NULL COMMENT 'Riot 매치 ID (예: KR_7654321)',
    queue_id   INT          NOT NULL COMMENT 'Riot 큐 ID (예: 420=솔로랭크, 440=자유랭크, 430=노말)',
    game_start BIGINT       NOT NULL COMMENT '게임 시작 시각 (Unix epoch ms)',
    raw_data   JSON         NOT NULL COMMENT 'Riot Match API 원본 응답 전체',
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '최초 저장 시각',
    PRIMARY KEY (match_id)
) COMMENT = 'Riot 매치 원본 데이터 캐시';
