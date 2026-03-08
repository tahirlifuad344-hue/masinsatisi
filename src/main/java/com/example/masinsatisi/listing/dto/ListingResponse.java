package com.example.masinsatisi.listing.dto;

import com.example.masinsatisi.listing.FuelType;
import com.example.masinsatisi.listing.ListingStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListingResponse {
    private final Long id;
    private final String brand;
    private final String model;
    private final Integer year;
    private final BigDecimal engineSize;
    private final String color;
    private final FuelType fuelType;
    private final BigDecimal price;
    private final ListingStatus status;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final UserSummaryResponse user;
    private final List<ListingImageResponse> images;
}
