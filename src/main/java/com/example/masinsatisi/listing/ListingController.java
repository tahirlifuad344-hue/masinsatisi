package com.example.masinsatisi.listing;

import com.example.masinsatisi.common.PageResponse;
import com.example.masinsatisi.listing.dto.ListingCreateRequest;
import com.example.masinsatisi.listing.dto.ListingResponse;
import com.example.masinsatisi.listing.dto.ListingUpdateRequest;
import com.example.masinsatisi.security.SecurityUtils;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ListingResponse createListing(
            @Valid @RequestPart("data") ListingCreateRequest data,
            @RequestPart("images") List<MultipartFile> images) {
        Long userId = SecurityUtils.getCurrentUserId();
        Listing listing = listingService.createListing(userId, data, images);
        return ListingMapper.toResponse(listing);
    }

    @GetMapping
    public PageResponse<ListingResponse> getListings(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false) BigDecimal engineFrom,
            @RequestParam(required = false) BigDecimal engineTo,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) FuelType fuelType,
            @RequestParam(required = false) BigDecimal priceFrom,
            @RequestParam(required = false) BigDecimal priceTo,
            @RequestParam(defaultValue = "DATE_DESC") ListingSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize, resolveSort(sort));

        ListingFilter filter = ListingFilter.builder()
                .brand(brand)
                .model(model)
                .yearFrom(yearFrom)
                .yearTo(yearTo)
                .engineFrom(engineFrom)
                .engineTo(engineTo)
                .color(color)
                .fuelType(fuelType)
                .priceFrom(priceFrom)
                .priceTo(priceTo)
                .build();

        Page<Listing> listings = listingService.findListings(filter, pageable);
        List<ListingResponse> content = listings.getContent().stream()
                .map(ListingMapper::toResponse)
                .toList();
        return new PageResponse<>(content, listings.getNumber(), listings.getSize(),
                listings.getTotalElements(), listings.getTotalPages());
    }

    @GetMapping("/{id}")
    public ListingResponse getListing(@PathVariable Long id) {
        return ListingMapper.toResponse(listingService.getListing(id));
    }

    @PutMapping("/{id}")
    public ListingResponse updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Listing listing = listingService.updateListing(id, userId, request);
        return ListingMapper.toResponse(listing);
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ListingResponse addImages(
            @PathVariable Long id,
            @RequestPart("images") List<MultipartFile> images) {
        Long userId = SecurityUtils.getCurrentUserId();
        Listing listing = listingService.addImages(id, userId, images);
        return ListingMapper.toResponse(listing);
    }

    @DeleteMapping("/{id}")
    public void deleteListing(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        listingService.deleteListing(id, userId);
    }

    private Sort resolveSort(ListingSort sort) {
        return switch (sort) {
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price");
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price");
            case DATE_ASC -> Sort.by(Sort.Direction.ASC, "createdAt");
            case DATE_DESC -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }
}
