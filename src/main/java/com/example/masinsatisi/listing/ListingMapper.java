package com.example.masinsatisi.listing;

import com.example.masinsatisi.listing.dto.ListingImageResponse;
import com.example.masinsatisi.listing.dto.ListingResponse;
import com.example.masinsatisi.listing.dto.UserSummaryResponse;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class ListingMapper {

    private ListingMapper() {
    }

    public static ListingResponse toResponse(Listing listing) {
        List<ListingImageResponse> images = listing.getImages().stream()
                .sorted(Comparator.comparing(ListingImage::isPrimary).reversed()
                        .thenComparing(ListingImage::getId))
                .map(image -> new ListingImageResponse(
                        image.getId(),
                        image.getUrl(),
                        image.isPrimary(),
                        image.getContentType(),
                        image.getSizeBytes(),
                        image.getCreatedAt()))
                .collect(Collectors.toList());

        UserSummaryResponse user = new UserSummaryResponse(
                listing.getUser().getId(),
                listing.getUser().getFullName(),
                listing.getUser().getPhone());

        return new ListingResponse(
                listing.getId(),
                listing.getBrand(),
                listing.getModel(),
                listing.getYear(),
                listing.getEngineSize(),
                listing.getColor(),
                listing.getFuelType(),
                listing.getPrice(),
                listing.getStatus(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),
                user,
                images
        );
    }
}
