package com.altis.library_backend.auth.models.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterRequestDTO(
        @NotBlank(message = "the field cannot be null")
        String nameCompleted,

        @NotBlank(message = "the field cannot be null")
        @Email
        String email,

        @NotBlank(message = "the field cannot be null")
        @Pattern(regexp = "^\\(\\d{2}\\)\\s9\\d{4}-\\d{4}$")
        String phone,

        @NotBlank(message = "the field cannot be null")
        String address,

        @NotBlank(message = "the field cannot be null")
        @Pattern(regexp = "^\\d{11}|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")
        String cpf,

        @NotNull(message = "the field cannot be null")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateBirth,

        @NotBlank(message = "the field cannot be null")
        String password
) {}
