package com.example.masinsatisi.favorite;

import com.example.masinsatisi.listing.Listing;
import com.example.masinsatisi.listing.ListingRepository;
import com.example.masinsatisi.listing.ListingService;
import com.example.masinsatisi.listing.dto.ListingResponse;
import com.example.masinsatisi.user.User;
import com.example.masinsatisi.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingService listingService;

    @Transactional
    public void add(Long userId, Long listingId) {
        if (favoriteRepository.existsByUserIdAndListingId(userId, listingId)) return;
        User user = userRepository.getReferenceById(userId);
        Listing listing = listingRepository.getReferenceById(listingId);
        favoriteRepository.save(Favorite.builder()
                .user(user).listing(listing).build());
    }

    @Transactional
    public void remove(Long userId, Long listingId) {
        favoriteRepository.deleteByUserIdAndListingId(userId, listingId);
    }

    @Transactional(readOnly = true)
    public List<ListingResponse> getMyFavorites(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(f -> listingService.toResponse(f.getListing(), userId))
                .toList();
    }
}