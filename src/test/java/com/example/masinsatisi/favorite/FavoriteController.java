package com.example.masinsatisi.favorite;

import com.example.masinsatisi.listing.dto.ListingResponse;
import com.example.masinsatisi.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{listingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> add(@PathVariable Long listingId) {
        favoriteService.add(SecurityUtils.getCurrentUserId(), listingId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{listingId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> remove(@PathVariable Long listingId) {
        favoriteService.remove(SecurityUtils.getCurrentUserId(), listingId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ListingResponse>> getMyFavorites() {
        return ResponseEntity.ok(
                favoriteService.getMyFavorites(SecurityUtils.getCurrentUserId()));
    }
}