package com.example.masinsatisi.listing;

import com.example.masinsatisi.exception.ForbiddenException;
import com.example.masinsatisi.listing.dto.ListingCreateRequest;
import com.example.masinsatisi.storage.ImageStorageService;
import com.example.masinsatisi.storage.StoredFile;
import com.example.masinsatisi.user.User;
import com.example.masinsatisi.user.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private ListingImageRepository listingImageRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ListingService listingService;

    @Test
    void createListing_setsPrimaryImage() {
        User user = User.builder().id(1L).fullName("Test").phone("123").email("a@b.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(imageStorageService.store(any())).thenReturn(
                new StoredFile("a.jpg", "uploads/a.jpg", "/uploads/a.jpg", "image/jpeg", 10L),
                new StoredFile("b.jpg", "uploads/b.jpg", "/uploads/b.jpg", "image/jpeg", 11L)
        );

        when(listingImageRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ListingCreateRequest request = new ListingCreateRequest();
        request.setBrand("BMW");
        request.setModel("X5");
        request.setYear(2020);
        request.setEngineSize(new BigDecimal("3.0"));
        request.setColor("Black");
        request.setFuelType(FuelType.GASOLINE);
        request.setPrice(new BigDecimal("50000"));

        MockMultipartFile file1 = new MockMultipartFile("images", "a.jpg", "image/jpeg", "a".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("images", "b.jpg", "image/jpeg", "b".getBytes());

        Listing listing = listingService.createListing(1L, request, List.of(file1, file2));

        ArgumentCaptor<List<ListingImage>> captor = ArgumentCaptor.forClass(List.class);
        verify(listingImageRepository).saveAll(captor.capture());
        List<ListingImage> savedImages = captor.getValue();

        Assertions.assertEquals(2, savedImages.size());
        Assertions.assertTrue(savedImages.get(0).isPrimary());
        Assertions.assertEquals(2, listing.getImages().size());
    }

    @Test
    void deleteImage_throwsWhenNotOwner() {
        User owner = User.builder().id(1L).fullName("Owner").phone("1").email("o@o.com").build();
        Listing listing = new Listing();
        listing.setUser(owner);

        ListingImage image = new ListingImage();
        image.setId(10L);
        image.setListing(listing);

        when(listingImageRepository.findById(10L)).thenReturn(Optional.of(image));

        Assertions.assertThrows(ForbiddenException.class, () -> listingService.deleteImage(10L, 2L));
    }
}
