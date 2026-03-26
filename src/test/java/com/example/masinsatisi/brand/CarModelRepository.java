package com.example.masinsatisi.brand;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {
    List<CarModel> findByBrandIdOrderByNameAsc(Long brandId);
}