CREATE TABLE users
(
    id          BIGINT       NOT NULL AUTO_INCREMENT   COMMENT '사용자 고유 식별자',
    email       VARCHAR(255) NOT NULL                  COMMENT '이메일 주소',
    social_id   VARCHAR(100) NOT NULL                  COMMENT '소셜 제공자로부터 발급된 고유 ID (예: Google sub)',
    social_type VARCHAR(20)  NOT NULL                  COMMENT '소셜 로그인 제공자 유형 (예: GOOGLE)',
    deleted_at  TIMESTAMP    NULL                      COMMENT '탈퇴 처리 시각. NULL이면 정상 회원 (soft delete)',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP                    COMMENT '회원가입 시각',
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 수정 시각',
    PRIMARY KEY (id),
    UNIQUE KEY uq_email (email),
    UNIQUE KEY uq_social_id (social_id)
) COMMENT = '사용자 정보';
