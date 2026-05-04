package com.sgm.hansimapi.domain.user;

import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);
    Optional<User> findBySocialIdAndSocialType(String socialId, SocialType socialType);
    User save(User user);
    void softDelete(Long id);
    void updateSummoner(Long id, String riotGameName, String riotTagLine, String riotPuuid);
}
