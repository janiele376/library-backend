
package com.altis.library_backend.books.models.dtos;

public record UpdateResponseDTO(
        Long id,
        String title,
        String genre,
        String releaseDate,
        Long publisherId,
        String publisherName,
        Integer quantity
) {}
