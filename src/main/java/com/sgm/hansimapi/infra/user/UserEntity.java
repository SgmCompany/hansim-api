package com.sgm.hansimapi.infra.user;

import com.sgm.hansimapi.domain.user.SocialType;
import com.sgm.hansimapi.domain.user.User;
import com.sgm.hansimapi.domain.user.WorkType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/** 사용자 정보 */
@Entity
@Table(name = "users")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class UserEntity {

    /** 사용자 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 이메일 주소 */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** 소셜 제공자로부터 발급된 고유 ID (예: Google sub) */
    @Column(name = "social_id", nullable = false, unique = true, length = 100)
    private String socialId;

    /** 소셜 로그인 제공자 유형 (예: GOOGLE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "social_type", nullable = false, length = 20)
    private SocialType socialType;

    /** 근무 유형 */
    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", length = 20)
    private WorkType workType;

    /** 급여 (원 단위). 연봉제=연봉, 시급제=시급. STUDENT/UNEMPLOYED는 null */
    @Column(name = "salary_amount")
    private Integer salaryAmount;

    /** 연동된 Riot 게임 이름 */
    @Column(name = "riot_game_name", length = 100)
    private String riotGameName;

    /** 연동된 Riot 태그라인 */
    @Column(name = "riot_tag_line", length = 20)
    private String riotTagLine;

    /** 연동된 Riot PUUID */
    @Column(name = "riot_puuid", unique = true, length = 78)
    private String riotPuuid;

    /** 탈퇴 처리 시각. NULL이면 정상 회원 (soft delete) */
    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private Instant deletedAt;

    /** 회원가입 시각 */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    /** 최종 수정 시각 */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private Instant updatedAt;

    protected UserEntity() {}

    private UserEntity(String email, String socialId, SocialType socialType) {
        this.email      = email;
        this.socialId   = socialId;
        this.socialType = socialType;
    }

    public static UserEntity from(User user) {
        return new UserEntity(user.getEmail(), user.getSocialId(), user.getSocialType());
    }

    public void updateProfile(WorkType workType, Integer salaryAmount) {
        this.workType      = workType;
        this.salaryAmount  = salaryAmount;
    }

    public void linkSummoner(String riotGameName, String riotTagLine, String riotPuuid) {
        this.riotGameName = riotGameName;
        this.riotTagLine  = riotTagLine;
        this.riotPuuid    = riotPuuid;
    }

    public void unlinkSummoner() {
        this.riotGameName = null;
        this.riotTagLine  = null;
        this.riotPuuid    = null;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.email     = "DELETED_" + this.id + "_" + this.email;
        this.socialId  = "DELETED_" + this.id + "_" + this.socialId;
    }

    public User toDomain() {
        return User.of(id, email, socialId, socialType,
                workType, salaryAmount,
                riotGameName, riotTagLine, riotPuuid, deletedAt);
    }
}
