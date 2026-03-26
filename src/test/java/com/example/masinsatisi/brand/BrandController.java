package com.example.masinsatisi.brand;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandRepository brandRepository;
    private final CarModelRepository carModelRepository;

    @GetMapping
    public ResponseEntity<List<Brand>> getAllBrands() {
        return ResponseEntity.ok(brandRepository.findAllByOrderByNameAsc());
    }

    @GetMapping("/{brandId}/models")
    public ResponseEntity<List<CarModel>> getModels(@PathVariable Long brandId) {
        return ResponseEntity.ok(carModelRepository.findByBrandIdOrderByNameAsc(brandId));
    }
}