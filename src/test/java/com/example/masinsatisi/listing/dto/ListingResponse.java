package com.example.masinsatisi.listing.dto;

import com.example.masinsatisi.listing.DriveType;
import com.example.masinsatisi.listing.FuelType;
import com.example.masinsatisi.listing.ListingStatus;
import com.example.masinsatisi.listing.TransmissionType;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ListingResponse {
    private Long id;
    private String brandName;
    private String modelName;
    private String cityName;
    private Integer year;
    private BigDecimal price;
    private String currency;
    private Integer mileage;
    private FuelType fuelType;
    private TransmissionType transmission;
    private DriveType driveType;
    private String color;
    private BigDecimal engineSize;
    private Integer enginePower;
    private String description;
    private ListingStatus status;
    private Integer viewCount;
    private Boolean barter;
    private Boolean onCredit;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private Long userId;
    private String userName;
    private String userPhone;
    private boolean isFavorite;
}