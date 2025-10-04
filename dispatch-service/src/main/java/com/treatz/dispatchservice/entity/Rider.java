package com.treatz.dispatchservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "riders")
public class Rider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId; // The ID of the user from the Auth Service

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean isAvailable = true;
}