package com.example.masinsatisi.listing;

import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    public static Specification<Listing> withFilter(ListingFilter filter) {
        Specification<Listing> spec = (root, query, cb) -> cb.equal(root.get("status"), ListingStatus.ACTIVE);

        if (filter == null) {
            return spec;
        }

        if (hasText(filter.getBrand())) {
            String value = filter.getBrand().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("brand")), value));
        }
        if (hasText(filter.getModel())) {
            String value = filter.getModel().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("model")), value));
        }
        if (filter.getYearFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("year"), filter.getYearFrom()));
        }
        if (filter.getYearTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("year"), filter.getYearTo()));
        }
        if (filter.getEngineFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("engineSize"), filter.getEngineFrom()));
        }
        if (filter.getEngineTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("engineSize"), filter.getEngineTo()));
        }
        if (hasText(filter.getColor())) {
            String value = filter.getColor().toLowerCase();
            spec = spec.and((root, query, cb) -> cb.equal(cb.lower(root.get("color")), value));
        }
        if (filter.getFuelType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("fuelType"), filter.getFuelType()));
        }
        if (filter.getPriceFrom() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), filter.getPriceFrom()));
        }
        if (filter.getPriceTo() != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), filter.getPriceTo()));
        }

        return spec;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
