package com.example.masinsatisi.listing;

import com.example.masinsatisi.brand.Brand;
import com.example.masinsatisi.brand.BrandRepository;
import com.example.masinsatisi.brand.CarModel;
import com.example.masinsatisi.brand.CarModelRepository;
import com.example.masinsatisi.city.City;
import com.example.masinsatisi.city.CityRepository;
import com.example.masinsatisi.exception.ForbiddenException;
import com.example.masinsatisi.exception.NotFoundException;
import com.example.masinsatisi.favorite.FavoriteRepository;
import com.example.masinsatisi.listing.dto.ListingCreateRequest;
import com.example.masinsatisi.listing.dto.ListingFilterRequest;
import com.example.masinsatisi.listing.dto.ListingResponse;
import com.example.masinsatisi.storage.ImageStorageService;
import com.example.masinsatisi.storage.StoredFile;
import com.example.masinsatisi.user.User;
import com.example.masinsatisi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final CarModelRepository carModelRepository;
    private final CityRepository cityRepository;
    private final FavoriteRepository favoriteRepository;
    private final ImageStorageService imageStorageService;

    @Transactional
    public Listing createListing(Long userId, ListingCreateRequest req, List<MultipartFile> images) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("İstifadəçi tapılmadı"));

        Brand brand = brandRepository.findById(req.getBrandId())
                .orElseThrow(() -> new NotFoundException("Marka tapılmadı"));

        CarModel model = carModelRepository.findById(req.getModelId())
                .orElseThrow(() -> new NotFoundException("Model tapılmadı"));

        City city = req.getCityId() != null
                ? cityRepository.findById(req.getCityId()).orElse(null)
                : null;

        Listing listing = Listing.builder()
                .user(user)
                .brand(brand)
                .model(model)
                .city(city)
                .year(req.getYear())
                .price(req.getPrice())
                .currency(req.getCurrency() != null ? req.getCurrency() : "AZN")
                .mileage(req.getMileage())
                .fuelType(req.getFuelType())
                .transmission(req.getTransmission())
                .driveType(req.getDriveType())
                .color(req.getColor())
                .engineSize(req.getEngineSize())
                .enginePower(req.getEnginePower())
                .description(req.getDescription())
                .barter(req.getBarter() != null ? req.getBarter() : false)
                .onCredit(req.getOnCredit() != null ? req.getOnCredit() : false)
                .status(ListingStatus.ACTIVE)
                .viewCount(0)
                .build();

        listingRepository.save(listing);

        if (images != null && !images.isEmpty()) {
            boolean first = true;
            for (MultipartFile file : images) {
                StoredFile stored = imageStorageService.store(file);
                ListingImage img = new ListingImage();
                img.setListing(listing);
                img.setFileName(stored.getFileName());
                img.setFilePath(stored.getFilePath());
                img.setUrl(stored.getUrl());
                img.setPrimary(first);
                listing.getImages().add(img);
                first = false;
            }
            listingImageRepository.saveAll(listing.getImages());
        }

        return listing;
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> filterListings(ListingFilterRequest filter, Long currentUserId) {
        Pageable pageable = buildPageable(filter);
        Page<Listing> page = listingRepository.findWithFilters(
                filter.getBrandId(), filter.getModelId(), filter.getCityId(),
                filter.getYearFrom(), filter.getYearTo(),
                filter.getPriceFrom(), filter.getPriceTo(),
                filter.getMileageFrom(), filter.getMileageTo(),
                filter.getFuelType(), filter.getTransmission(), filter.getDriveType(),
                filter.getBarter(), filter.getOnCredit(),
                pageable);

        return page.map(l -> toResponse(l, currentUserId));
    }

    @Transactional
    public ListingResponse getById(Long id, Long currentUserId) {
        Listing listing = listingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Elan tapılmadı"));
        listing.setViewCount(listing.getViewCount() + 1);
        listingRepository.save(listing);
        return toResponse(listing, currentUserId);
    }

    @Transactional
    public void deleteListing(Long listingId, Long userId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Elan tapılmadı"));
        if (!listing.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bu elanı silmək icazəniz yoxdur");
        }
        listingRepository.delete(listing);
    }

    @Transactional
    public void deleteImage(Long imageId, Long userId) {
        ListingImage image = listingImageRepository.findById(imageId)
                .orElseThrow(() -> new NotFoundException("Şəkil tapılmadı"));
        if (!image.getListing().getUser().getId().equals(userId)) {
            throw new ForbiddenException("Bu şəkli silmək icazəniz yoxdur");
        }
        listingImageRepository.delete(image);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getMyListings(Long userId) {
        return listingRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(l -> toResponse(l, userId)).toList();
    }

    private Pageable buildPageable(ListingFilterRequest filter) {
        Sort sort = switch (filter.getSortBy() != null ? filter.getSortBy() : "date_desc") {
            case "price_asc"    -> Sort.by("price").ascending();
            case "price_desc"   -> Sort.by("price").descending();
            case "mileage_asc"  -> Sort.by("mileage").ascending();
            default             -> Sort.by("createdAt").descending();
        };
        return PageRequest.of(filter.getPage(), filter.getSize(), sort);
    }

    public ListingResponse toResponse(Listing l, Long currentUserId) {
        List<String> urls = l.getImages().stream().map(ListingImage::getUrl).toList();
        String primary = l.getImages().stream()
                .filter(ListingImage::isPrimary).map(ListingImage::getUrl)
                .findFirst().orElse(urls.isEmpty() ? null : urls.get(0));

        boolean fav = currentUserId != null &&
                favoriteRepository.existsByUserIdAndListingId(currentUserId, l.getId());

        return ListingResponse.builder()
                .id(l.getId())
                .brandName(l.getBrand().getName())
                .modelName(l.getModel().getName())
                .cityName(l.getCity() != null ? l.getCity().getName() : null)
                .year(l.getYear())
                .price(l.getPrice())
                .currency(l.getCurrency())
                .mileage(l.getMileage())
                .fuelType(l.getFuelType())
                .transmission(l.getTransmission())
                .driveType(l.getDriveType())
                .color(l.getColor())
                .engineSize(l.getEngineSize())
                .enginePower(l.getEnginePower())
                .description(l.getDescription())
                .status(l.getStatus())
                .viewCount(l.getViewCount())
                .barter(l.getBarter())
                .onCredit(l.getOnCredit())
                .createdAt(l.getCreatedAt())
                .imageUrls(urls)
                .primaryImageUrl(primary)
                .userId(l.getUser().getId())
                .userName(l.getUser().getFullName())
                .userPhone(l.getUser().getPhone())
                .isFavorite(fav)
                .build();
    }
}