package com.example.masinsatisi.listing;

import com.example.masinsatisi.brand.Brand;
import com.example.masinsatisi.brand.CarModel;
import com.example.masinsatisi.city.City;
import com.example.masinsatisi.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "listings")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private CarModel model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    private String currency; // AZN, USD

    @Column(nullable = false)
    private Integer mileage; // km

    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @ManyToOne
    @JoinColumn(name = "transmission_id")
    private TransmissionType transmission;

    @ManyToOne
    @JoinColumn(name = "drive_type_id")
    private DriveType driveType;

    private String color;

    @Column(precision = 3, scale = 1)
    private BigDecimal engineSize;

    private Integer enginePower; // at gücü

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.ACTIVE;

    private Integer viewCount = 0;

    private Boolean barter = false;   // dəyişmə var?
    private Boolean onCredit = false; // kreditlə?

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ListingImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}