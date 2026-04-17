package com.sgm.hansimapi.infra.google;

import com.sgm.hansimapi.domain.user.client.GoogleAuthClient;
import com.sgm.hansimapi.domain.user.client.GoogleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleAuthClientImpl implements GoogleAuthClient {

    // ID token 검증 엔드포인트: 프론트엔드 Google Sign-In SDK가 발급하는 ID token(JWT) 검증
    private static final String GOOGLE_TOKENINFO_URL =
            "https://www.googleapis.com/oauth2/v3/tokeninfo";

    private final RestTemplate restTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public GoogleUserInfo verify(String idToken) {
        URI uri = UriComponentsBuilder.fromUriString(GOOGLE_TOKENINFO_URL)
                .queryParam("id_token", idToken)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

        if (response == null || response.get("sub") == null) {
            throw new IllegalArgumentException("유효하지 않은 Google ID token입니다.");
        }

        String socialId = (String) response.get("sub");
        String email    = (String) response.get("email");

        return new GoogleUserInfo(socialId, email);
    }
}
