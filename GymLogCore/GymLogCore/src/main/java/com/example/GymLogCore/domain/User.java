package com.example.GymLogCore.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;


/*
Entity for representing user of system
*/
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(nullable = false, unique = true)
    String email;

    @Column(nullable = false, updatable = false)
    private DateFormat date;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Workout> trains = new ArrayList<>();
}
