package com.example.masinsatisi.listing;

import com.example.masinsatisi.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ListingImageController {

    private final ListingService listingService;

    @DeleteMapping("/{imageId}")
    public void deleteImage(@PathVariable Long imageId) {
        Long userId = SecurityUtils.getCurrentUserId();
        listingService.deleteImage(imageId, userId);
    }
}
