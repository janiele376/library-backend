
package com.altis.library_backend.dashboard.models.dtos;

public record MostRentedBookDTO(
        Long bookId,
        String title,
        String publisher,
        String releaseDate,
        Long quantity
) {
}