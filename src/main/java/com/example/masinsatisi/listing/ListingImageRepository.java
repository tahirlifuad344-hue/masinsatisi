package com.example.masinsatisi.listing;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {
    List<ListingImage> findByListingIdOrderByIdAsc(Long listingId);
}
