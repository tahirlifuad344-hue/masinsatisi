package com.example.masinsatisi.listing;

import com.example.masinsatisi.exception.BadRequestException;
import com.example.masinsatisi.exception.ForbiddenException;
import com.example.masinsatisi.exception.NotFoundException;
import com.example.masinsatisi.listing.dto.ListingCreateRequest;
import com.example.masinsatisi.listing.dto.ListingUpdateRequest;
import com.example.masinsatisi.storage.ImageStorageService;
import com.example.masinsatisi.storage.StoredFile;
import com.example.masinsatisi.user.User;
import com.example.masinsatisi.user.UserRepository;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ListingService {

    private static final int MAX_IMAGES = 10;

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Listing createListing(Long userId, ListingCreateRequest request, List<MultipartFile> images) {
        requireUserId(userId);
        validateYear(request.getYear());
        validateImages(images, true, 0);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Listing listing = new Listing();
        listing.setUser(user);
        applyRequest(listing, request);
        listing.setStatus(ListingStatus.ACTIVE);

        listingRepository.save(listing);

        List<ListingImage> storedImages = storeImages(listing, images, true);
        listing.getImages().addAll(storedImages);

        return listing;
    }

    public Page<Listing> findListings(ListingFilter filter, Pageable pageable) {
        return listingRepository.findAll(ListingSpecifications.withFilter(filter), pageable);
    }

    public Listing getListing(Long id) {
        return listingRepository.findById(id)
                .filter(listing -> listing.getStatus() == ListingStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    @Transactional
    public Listing updateListing(Long listingId, Long userId, ListingUpdateRequest request) {
        requireUserId(userId);
        validateYear(request.getYear());
        Listing listing = getListingForOwner(listingId, userId);
        applyRequest(listing, request);
        return listing;
    }

    @Transactional
    public Listing addImages(Long listingId, Long userId, List<MultipartFile> images) {
        requireUserId(userId);
        Listing listing = getListingForOwner(listingId, userId);
        int existing = listingImageRepository.findByListingIdOrderByIdAsc(listingId).size();
        validateImages(images, true, existing);

        boolean hasPrimary = listing.getImages().stream().anyMatch(ListingImage::isPrimary);
        List<ListingImage> storedImages = storeImages(listing, images, !hasPrimary);
        listing.getImages().addAll(storedImages);
        return listing;
    }

    @Transactional
    public void deleteListing(Long listingId, Long userId) {
        requireUserId(userId);
        Listing listing = getListingForOwner(listingId, userId);
        listing.setStatus(ListingStatus.DELETED);
    }

    @Transactional
    public void deleteImage(Long imageId, Long userId) {
        requireUserId(userId);
        ListingImage image = listingImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        Listing listing = image.getListing();
        if (!listing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can delete this image");
        }

        boolean wasPrimary = image.isPrimary();
        listingImageRepository.delete(image);
        imageStorageService.deleteIfExists(image.getFilePath());

        if (wasPrimary) {
            List<ListingImage> remaining = listingImageRepository.findByListingIdOrderByIdAsc(listing.getId());
            if (!remaining.isEmpty()) {
                ListingImage newPrimary = remaining.get(0);
                newPrimary.setPrimary(true);
                listingImageRepository.save(newPrimary);
            }
        }
    }

    private Listing getListingForOwner(Long listingId, Long userId) {
        Listing listing = getListing(listingId);
        if (!listing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Only owner can modify this listing");
        }
        return listing;
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new ForbiddenException("Authentication required");
        }
    }

    private void applyRequest(Listing listing, ListingCreateRequest request) {
        listing.setBrand(request.getBrand().trim());
        listing.setModel(request.getModel().trim());
        listing.setYear(request.getYear());
        listing.setEngineSize(request.getEngineSize());
        listing.setColor(request.getColor().trim());
        listing.setFuelType(request.getFuelType());
        listing.setPrice(request.getPrice());
    }

    private void applyRequest(Listing listing, ListingUpdateRequest request) {
        listing.setBrand(request.getBrand().trim());
        listing.setModel(request.getModel().trim());
        listing.setYear(request.getYear());
        listing.setEngineSize(request.getEngineSize());
        listing.setColor(request.getColor().trim());
        listing.setFuelType(request.getFuelType());
        listing.setPrice(request.getPrice());
    }

    private void validateYear(Integer year) {
        int currentYear = Year.now().getValue();
        if (year == null || year < 1900 || year > currentYear) {
            throw new BadRequestException("Year must be between 1900 and " + currentYear);
        }
    }

    private void validateImages(List<MultipartFile> images, boolean required, int existing) {
        if ((images == null || images.isEmpty()) && required) {
            throw new BadRequestException("At least one image is required");
        }
        if (images != null && existing + images.size() > MAX_IMAGES) {
            throw new BadRequestException("Maximum " + MAX_IMAGES + " images allowed");
        }
    }

    private List<ListingImage> storeImages(Listing listing, List<MultipartFile> images, boolean markFirstPrimary) {
        List<ListingImage> stored = new ArrayList<>();
        if (images == null) {
            return stored;
        }
        boolean primarySet = listing.getImages().stream().anyMatch(ListingImage::isPrimary);
        for (MultipartFile file : images) {
            StoredFile storedFile = imageStorageService.store(file);
            ListingImage image = new ListingImage();
            image.setListing(listing);
            image.setFileName(storedFile.getFileName());
            image.setFilePath(storedFile.getFilePath());
            image.setUrl(storedFile.getUrl());
            image.setContentType(storedFile.getContentType());
            image.setSizeBytes(storedFile.getSizeBytes());

            if (!primarySet && markFirstPrimary && stored.isEmpty()) {
                image.setPrimary(true);
                primarySet = true;
            }
            stored.add(image);
        }
        return listingImageRepository.saveAll(stored);
    }
}
