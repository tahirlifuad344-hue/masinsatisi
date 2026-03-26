package com.example.masinsatisi.listing.dto;

import com.example.masinsatisi.listing.DriveType;
import com.example.masinsatisi.listing.FuelType;
import com.example.masinsatisi.listing.TransmissionType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ListingFilterRequest {
    private Long brandId;
    private Long modelId;
    private Long cityId;
    private Integer yearFrom;
    private Integer yearTo;
    private BigDecimal priceFrom;
    private BigDecimal priceTo;
    private Integer mileageFrom;
    private Integer mileageTo;
    private FuelType fuelType;
    private TransmissionType transmission;
    private DriveType driveType;
    private String color;
    private Boolean barter;
    private Boolean onCredit;
    private String sortBy;   // price_asc, price_desc, date_desc, mileage_asc
    private int page = 0;
    private int size = 20;
}