package com.altis.library_backend.publishers.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CNPJ;

public record PublisherRequestDTO(
        @NotBlank(message = "The full name is mandatory.")
        String name,

        @NotBlank(message = "The full cnpj is mandatory.")
        @Pattern(regexp = "^\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}$")
        @CNPJ
        String cnpj,

        @NotBlank(message = "The full email is mandatory.")
        @Email(message = "the format is invalid")
        String email,

        @NotBlank(message = "The full phone is mandatory.")
        @Pattern(regexp = "^\\(\\d{2}\\)\\s9\\d{4}-\\d{4}$")
        String phone,

        @NotBlank(message = "The full address is mandatory.")
        String address
) {}
