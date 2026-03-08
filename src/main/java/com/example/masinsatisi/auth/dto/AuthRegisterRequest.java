package com.example.masinsatisi.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRegisterRequest {

    @NotBlank
    @Size(max = 150)
    private String fullName;

    @NotBlank
    @Size(max = 40)
    private String phone;

    @NotBlank
    @Email
    @Size(max = 190)
    private String email;

    @NotBlank
    @Size(min = 6, max = 128)
    private String password;
}
