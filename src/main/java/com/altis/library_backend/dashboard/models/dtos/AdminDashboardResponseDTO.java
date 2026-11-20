
package com.altis.library_backend.dashboard.models.dtos;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public record AdminDashboardResponseDTO(
        Long onTimeRentals,
        Long nearDueRentals,
        Long overdueRentals,
        List<DashboardRentalDTO> lastRentals,
        Long totalRentals,
        Long totalBooks,
        Long totalPublishers,
        MostRentedBookDTO mostRentedBook,
        Page<DashboardBookDTO> availableBooks,
        Map<Long, Long> activeRentalsByBook
) {
}
