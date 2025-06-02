package com.example.springaichatbot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Product {

    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    private String name;

    private String image;

    private double price;

    private String title;

    @Column(length = 1000)
    private String description;

    private String size;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
