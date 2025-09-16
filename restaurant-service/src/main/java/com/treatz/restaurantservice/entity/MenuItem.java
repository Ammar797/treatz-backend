package com.treatz.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@Data
@Entity
@Table(name = "menu_items")
@EntityListeners(AuditingEntityListener.class)
public class MenuItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "is_available")
    private boolean available = true;

    @Column(name = "category")
    private String category; // e.g., "STARTER", "MAIN_COURSE", "DESSERT", "BEVERAGE"

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

}