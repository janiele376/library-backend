
package com.altis.library_backend.books.models.dtos;

import jakarta.validation.constraints.Min;

public record UpdateRequestDTO(

        String title,

        String genre,

        String releaseDate,

        Long publisherId,

        @Min(value = 0, message = "A quantidade não pode ser negativa.")
        Integer quantity

) {}
