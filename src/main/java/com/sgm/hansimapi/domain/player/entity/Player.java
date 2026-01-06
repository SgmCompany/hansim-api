package com.sgm.hansimapi.domain.player.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String position;

    @Column
    private Integer jerseyNumber;

    @Column
    private Integer age;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column
    private LocalDateTime updatedAt;

    @Builder
    public Player(String name, String email, String position, Integer jerseyNumber, Integer age) {
        this.name = name;
        this.email = email;
        this.position = position;
        this.jerseyNumber = jerseyNumber;
        this.age = age;
    }

    public void update(String name, String email, String position, Integer jerseyNumber, Integer age) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
        if (position != null) this.position = position;
        if (jerseyNumber != null) this.jerseyNumber = jerseyNumber;
        if (age != null) this.age = age;
    }
}