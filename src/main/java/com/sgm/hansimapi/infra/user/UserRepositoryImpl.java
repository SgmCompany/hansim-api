package com.sgm.hansimapi.infra.user;

import com.sgm.hansimapi.domain.user.SocialType;
import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findBySocialIdAndSocialType(String socialId, SocialType socialType) {
        return userJpaRepository.findBySocialIdAndSocialTypeAndDeletedAtIsNull(socialId, socialType)
                .map(UserEntity::toDomain);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(UserEntity.from(user)).toDomain();
    }

    @Override
    public void softDelete(Long id) {
        userJpaRepository.findById(id).ifPresent(UserEntity::softDelete);
    }

    @Override
    public void updateSummoner(Long id, String riotGameName, String riotTagLine, String riotPuuid) {
        userJpaRepository.findById(id).ifPresent(entity -> {
            if (riotPuuid == null) {
                entity.unlinkSummoner();
            } else {
                entity.linkSummoner(riotGameName, riotTagLine, riotPuuid);
            }
        });
    }
}
