package com.example.GymLogCore.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/*
 represents train
*/

@Entity
@Table(name = "workouts")
@Getter
@Setter
@NoArgsConstructor
public class Workout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String type;

    private String description;

    @Column(nullable = false)
    private LocalDateTime date;

    private int durationMinutes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
