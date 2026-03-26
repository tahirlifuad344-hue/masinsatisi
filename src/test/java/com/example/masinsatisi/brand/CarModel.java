package com.example.masinsatisi.brand;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "car_models")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CarModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;
}