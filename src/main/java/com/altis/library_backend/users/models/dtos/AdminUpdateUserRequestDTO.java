
package com.altis.library_backend.users.models.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

public record AdminUpdateUserRequestDTO(
        @Size(min = 3, max = 150)
        @Pattern(
                regexp = "^[a-zA-ZÀ-ÿ]+(?:\\s+[a-zA-ZÀ-ÿ]+)+$"
        )
        String nameCompleted,

        @Email
        String email,

        @Pattern(regexp = "^\\(\\d{2}\\)\\s9\\d{4}-\\d{4}$")
        String phone,

        @Pattern(
                regexp = "^(?:\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$"
        )
        @CPF
        String cpf,

        String address,

        @Past
        LocalDate dateBirth
) {}
