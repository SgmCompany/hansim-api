package com.sgm.hansimapi.domain.user;

import java.time.Instant;

public class User {

    private final Long id;
    private final String email;
    private final String socialId;
    private final SocialType socialType;
    private final String riotGameName;
    private final String riotTagLine;
    private final String riotPuuid;
    private final Instant deletedAt;

    private User(Long id, String email, String socialId, SocialType socialType,
                 String riotGameName, String riotTagLine, String riotPuuid, Instant deletedAt) {
        this.id           = id;
        this.email        = email;
        this.socialId     = socialId;
        this.socialType   = socialType;
        this.riotGameName = riotGameName;
        this.riotTagLine  = riotTagLine;
        this.riotPuuid    = riotPuuid;
        this.deletedAt    = deletedAt;
    }

    public static User create(String email, String socialId, SocialType socialType) {
        return new User(null, email, socialId, socialType, null, null, null, null);
    }

    public static User of(Long id, String email, String socialId, SocialType socialType,
                          String riotGameName, String riotTagLine, String riotPuuid, Instant deletedAt) {
        return new User(id, email, socialId, socialType, riotGameName, riotTagLine, riotPuuid, deletedAt);
    }

    public boolean isDeleted()   { return deletedAt != null; }
    public boolean hasSummoner() { return riotPuuid != null; }

    public Long getId()               { return id; }
    public String getEmail()          { return email; }
    public String getSocialId()       { return socialId; }
    public SocialType getSocialType() { return socialType; }
    public String getRiotGameName()   { return riotGameName; }
    public String getRiotTagLine()    { return riotTagLine; }
    public String getRiotPuuid()      { return riotPuuid; }
}
