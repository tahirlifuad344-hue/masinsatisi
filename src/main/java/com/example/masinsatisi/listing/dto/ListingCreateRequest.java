package com.example.masinsatisi.listing.dto;

import com.example.masinsatisi.listing.FuelType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListingCreateRequest {

    @NotBlank
    @Size(max = 100)
    private String brand;

    @NotBlank
    @Size(max = 100)
    private String model;

    @NotNull
    @Min(1900)
    private Integer year;

    @NotNull
    @DecimalMin("0.1")
    private BigDecimal engineSize;

    @NotBlank
    @Size(max = 60)
    private String color;

    @NotNull
    private FuelType fuelType;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal price;
}
