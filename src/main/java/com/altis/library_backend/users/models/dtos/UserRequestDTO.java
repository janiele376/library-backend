package com.altis.library_backend.users.models.dtos;
import jakarta.persistence.Id;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
public record UserRequestDTO(
        @NotBlank(message = "The full name is mandatory.")
        @Size(min = 3, max = 150, message = "The name must be between 3 and 150 characters long.")
        @Pattern(
                regexp = "^[a-zA-ZÀ-ÿ]+(?:\\s+[a-zA-ZÀ-ÿ]+)+$",
                message = "Enter your full name (first and last name), containing only letters."
        )
        String nameCompleted,

        @Email(message = "the format is invalid")
        @NotBlank(message = "The email field is required")
        String email,

        @Pattern(regexp = "^\\(\\d{2}\\)\\s9\\d{4}-\\d{4}$")
        @NotBlank(message = "The phone field is required")
        String phone,

        @Pattern(regexp = "^\\d{11}|^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$")
        @NotBlank(message = "The cpf field is required")
        @CPF
        String cpf,

        @NotBlank(message = "The address field is required")
        String address,

        @NotNull(message = "The dateBirth field is required")
        @Past(message = "Birth data must be in the past")
        LocalDate dateBirth,

        @NotBlank(message = "The password field is required")
        @Size(min = 8, message = "The password field require 8 characters")
        String password
) {}
