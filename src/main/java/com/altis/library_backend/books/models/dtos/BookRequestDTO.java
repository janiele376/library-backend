
package com.altis.library_backend.books.models.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequestDTO(

        @NotBlank
        @Size(max = 13)
        String isbn,

        @NotBlank
        String title,

        @NotBlank
        String genre,

        @NotBlank
        String releaseDate,

        Long publisherId,

        String publisherName,

        @NotNull(message = "A quantidade é obrigatória.")
        @Min(value = 0, message = "A quantidade não pode ser negativa.")
        Integer quantity

) {}
