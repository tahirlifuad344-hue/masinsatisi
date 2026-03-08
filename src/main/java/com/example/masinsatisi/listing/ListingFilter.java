package com.example.masinsatisi.listing;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListingFilter {
    private final String brand;
    private final String model;
    private final Integer yearFrom;
    private final Integer yearTo;
    private final BigDecimal engineFrom;
    private final BigDecimal engineTo;
    private final String color;
    private final FuelType fuelType;
    private final BigDecimal priceFrom;
    private final BigDecimal priceTo;
}
