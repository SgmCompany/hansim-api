package com.sgm.hansimapi.domain.user.client;

import com.sgm.hansimapi.domain.user.SocialType;

public class GoogleUserInfo {

    private final String socialId;  // Google sub (고유 식별자)
    private final String email;
    private final SocialType socialType = SocialType.GOOGLE;

    public GoogleUserInfo(String socialId, String email) {
        this.socialId = socialId;
        this.email = email;
    }

    public String getSocialId()       { return socialId; }
    public String getEmail()          { return email; }
    public SocialType getSocialType() { return socialType; }
}
