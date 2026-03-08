package com.example.masinsatisi.listing.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListingImageResponse {
    private final Long id;
    private final String url;
    private final boolean primary;
    private final String contentType;
    private final long sizeBytes;
    private final Instant createdAt;
}
