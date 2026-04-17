package com.sgm.hansimapi.infra.user;

import com.sgm.hansimapi.domain.user.SocialType;
import com.sgm.hansimapi.domain.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "social_id", nullable = false, unique = true, length = 100)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private Instant deletedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Instant updatedAt;

    protected UserEntity() {}

    private UserEntity(String email, String socialId, SocialType socialType) {
        this.email = email;
        this.socialId = socialId;
        this.socialType = socialType;
    }

    public static UserEntity from(User user) {
        return new UserEntity(user.getEmail(), user.getSocialId(), user.getSocialType());
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.email    = "DELETED_" + this.id + "_" + this.email;
        this.socialId = "DELETED_" + this.id + "_" + this.socialId;
    }

    public User toDomain() {
        return User.of(id, email, socialId, socialType, deletedAt);
    }
}
