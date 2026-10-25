
package com.altis.library_backend.dashboard.models.dtos;

import org.springframework.data.domain.Page;

import java.util.List;

public record UserDashboardResponseDTO(
        List<DashboardRentalDTO> lastRentals,
        Long onTimeRentals,
        Long nearDueRentals,
        Long overdueRentals,
        String mostRentedBook,
        MostRentedBookDTO mostRentedBookDetails,
        Page<DashboardBookDTO> availableBooks
) {
}
