package com.testtask.taskmanagement.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

/*    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Task> ownedTasks;

    @OneToMany(mappedBy = "executor", fetch = FetchType.LAZY)
    private List<Task> workTasks;*/
}
