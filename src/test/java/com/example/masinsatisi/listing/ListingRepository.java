package com.example.masinsatisi.listing;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ListingRepository extends JpaRepository<Listing, Long> {

    List<Listing> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("""
        SELECT l FROM Listing l
        JOIN FETCH l.brand JOIN FETCH l.model
        LEFT JOIN FETCH l.city LEFT JOIN FETCH l.images
        WHERE l.status = 'ACTIVE'
          AND (:brandId    IS NULL OR l.brand.id    = :brandId)
          AND (:modelId    IS NULL OR l.model.id    = :modelId)
          AND (:cityId     IS NULL OR l.city.id     = :cityId)
          AND (:yearFrom   IS NULL OR l.year        >= :yearFrom)
          AND (:yearTo     IS NULL OR l.year        <= :yearTo)
          AND (:priceFrom  IS NULL OR l.price       >= :priceFrom)
          AND (:priceTo    IS NULL OR l.price       <= :priceTo)
          AND (:mileFrom   IS NULL OR l.mileage     >= :mileFrom)
          AND (:mileTo     IS NULL OR l.mileage     <= :mileTo)
          AND (:fuelType   IS NULL OR l.fuelType    = :fuelType)
          AND (:transmission IS NULL OR l.transmission = :transmission)
          AND (:driveType  IS NULL OR l.driveType   = :driveType)
          AND (:barter     IS NULL OR l.barter      = :barter)
          AND (:onCredit   IS NULL OR l.onCredit    = :onCredit)
    """)
    Page<Listing> findWithFilters(
            @Param("brandId")      Long brandId,
            @Param("modelId")      Long modelId,
            @Param("cityId")       Long cityId,
            @Param("yearFrom")     Integer yearFrom,
            @Param("yearTo")       Integer yearTo,
            @Param("priceFrom")    BigDecimal priceFrom,
            @Param("priceTo")      BigDecimal priceTo,
            @Param("mileFrom")     Integer mileFrom,
            @Param("mileTo")       Integer mileTo,
            @Param("fuelType")     FuelType fuelType,
            @Param("transmission") TransmissionType transmission,
            @Param("driveType")    DriveType driveType,
            @Param("barter")       Boolean barter,
            @Param("onCredit")     Boolean onCredit,
            Pageable pageable);
}