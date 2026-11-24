package com.altis.library_backend.publishers.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;

public record UpdateRequestDTO(
        String name,

        @Email(message = "the format is invalid")
        String email,

        String phone,

        String address
) {}