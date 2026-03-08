package com.example.masinsatisi.listing.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSummaryResponse {
    private final Long id;
    private final String fullName;
    private final String phone;
}
