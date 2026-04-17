package com.sgm.hansimapi.domain.user.port;

import com.sgm.hansimapi.domain.user.SocialType;

public class GoogleUserInfo {

    private final String socialId;
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
