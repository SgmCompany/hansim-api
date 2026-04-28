package com.sgm.hansimapi.infra.slack;

import com.sgm.hansimapi.config.SlackProperties;
import com.sgm.hansimapi.domain.notification.port.SlackClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackClientImpl implements SlackClient {

    private static final String POST_MESSAGE_URL = "https://slack.com/api/chat.postMessage";

    private final RestTemplate restTemplate;
    private final SlackProperties slackProperties;

    @Override
    public void sendMessage(String channel, String text) {
        String body = String.format("""
                {"channel":"%s","text":"%s"}
                """, channel, escapeJson(text)).strip();
        post(body);
    }

    @Override
    public void sendBlocks(String channel, String fallbackText, String blocksJson) {
        String body = String.format("""
                {"channel":"%s","text":"%s","blocks":%s}
                """, channel, escapeJson(fallbackText), blocksJson).strip();
        post(body);
    }

    @SuppressWarnings("unchecked")
    private void post(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(slackProperties.getBotToken());

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        Map<String, Object> response = restTemplate.postForObject(POST_MESSAGE_URL, entity, Map.class);

        if (response == null || !Boolean.TRUE.equals(response.get("ok"))) {
            String error = response != null ? (String) response.get("error") : "null response";
            log.error("Slack API 오류: {}", error);
            throw new SlackApiException("Slack 메시지 발송 실패: " + error);
        }
    }

    /** JSON 문자열 내 특수문자 이스케이프 */
    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}
