package com.example.masinsatisi.listing;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ListingRepository extends JpaRepository<Listing, Long>, JpaSpecificationExecutor<Listing> {

    @Override
    @EntityGraph(attributePaths = {"user", "images"})
    Page<Listing> findAll(Specification<Listing> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"user", "images"})
    Optional<Listing> findById(Long id);
}
