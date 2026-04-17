package com.sgm.hansimapi.domain.user;

import java.time.Instant;

public class User {

    private final Long id;
    private final String email;
    private final String socialId;
    private final SocialType socialType;
    private final Instant deletedAt;

    private User(Long id, String email, String socialId, SocialType socialType, Instant deletedAt) {
        this.id = id;
        this.email = email;
        this.socialId = socialId;
        this.socialType = socialType;
        this.deletedAt = deletedAt;
    }

    public static User create(String email, String socialId, SocialType socialType) {
        return new User(null, email, socialId, socialType, null);
    }

    public static User of(Long id, String email, String socialId, SocialType socialType, Instant deletedAt) {
        return new User(id, email, socialId, socialType, deletedAt);
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public Long getId()               { return id; }
    public String getEmail()          { return email; }
    public String getSocialId()       { return socialId; }
    public SocialType getSocialType() { return socialType; }
}
