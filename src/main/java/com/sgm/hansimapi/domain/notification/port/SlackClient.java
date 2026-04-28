package com.sgm.hansimapi.domain.notification.port;

/**
 * Slack 메시지 발송 포트.
 * 구현체는 infra/slack 레이어에 위치한다.
 */
public interface SlackClient {

    /**
     * 단순 텍스트 메시지를 발송한다.
     *
     * @param channel 채널 ID 또는 채널명 (예: "#general", "C0XXXXXXX")
     * @param text    발송할 텍스트
     */
    void sendMessage(String channel, String text);

    /**
     * Block Kit 리치 메시지를 발송한다.
     *
     * @param channel      채널 ID 또는 채널명
     * @param fallbackText Block Kit 미지원 환경에서 표시될 대체 텍스트
     * @param blocksJson   Block Kit blocks 배열 JSON 문자열
     */
    void sendBlocks(String channel, String fallbackText, String blocksJson);
}
