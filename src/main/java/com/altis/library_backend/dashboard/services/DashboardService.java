
package com.altis.library_backend.dashboard.services;

import com.altis.library_backend.books.models.entities.BookEntity;
import com.altis.library_backend.books.repositories.BookRepository;
import com.altis.library_backend.dashboard.models.dtos.AdminDashboardResponseDTO;
import com.altis.library_backend.dashboard.models.dtos.DashboardBookDTO;
import com.altis.library_backend.dashboard.models.dtos.DashboardRentalDTO;
import com.altis.library_backend.dashboard.models.dtos.MostRentedBookDTO;
import com.altis.library_backend.dashboard.models.dtos.UserDashboardResponseDTO;
import com.altis.library_backend.publishers.repositories.PublisherRepository;
import com.altis.library_backend.rentals.models.entities.RentalEntity;
import com.altis.library_backend.rentals.repositories.RentalRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final RentalRepository rentalRepository;
    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;

    public DashboardService(
            RentalRepository rentalRepository,
            BookRepository bookRepository,
            PublisherRepository publisherRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
    }

    @Transactional(readOnly = true)
    public UserDashboardResponseDTO getUserDashboard(
            Long userId,
            Pageable pageable
    ) {
        List<RentalEntity> userRentals =
                rentalRepository.findByUsersId_Id(userId);

        List<RentalEntity> lastRentals =
                rentalRepository.findTop5ByUsersId_IdOrderByCreatedAtDesc(userId);

        MostRentedBookDTO mostRentedBookDetails =
                findMostRentedBook(userRentals);

        String mostRentedBookTitle = mostRentedBookDetails == null
                ? null
                : mostRentedBookDetails.title();

        return new UserDashboardResponseDTO(
                toDashboardRentalDTOList(lastRentals),
                countRentalsByStatus(userRentals, "ON_TIME"),
                countRentalsByStatus(userRentals, "NEAR_DUE"),
                countRentalsByStatus(userRentals, "OVERDUE"),
                mostRentedBookTitle,
                mostRentedBookDetails,
                getAvailableBooks(pageable)
        );
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponseDTO getAdminDashboard(Pageable pageable) {

        List<RentalEntity> rentals = rentalRepository.findAll();

        List<RentalEntity> lastRentals =
                rentalRepository.findTop5ByOrderByCreatedAtDesc();

        return new AdminDashboardResponseDTO(
                countRentalsByStatus(rentals, "ON_TIME"),
                countRentalsByStatus(rentals, "NEAR_DUE"),
                countRentalsByStatus(rentals, "OVERDUE"),
                toDashboardRentalDTOList(lastRentals),
                rentalRepository.count(),
                bookRepository.count(),
                publisherRepository.count(),
                findMostRentedBook(rentals),
                getAvailableBooks(pageable),
                countActiveRentalsByBook(rentals)
        );
    }

    private long countRentalsByStatus(
            List<RentalEntity> rentals,
            String expectedStatus
    ) {
        return rentals.stream()
                .filter(rental ->
                        calculateStatus(rental).equals(expectedStatus)
                )
                .count();
    }

    private List<DashboardRentalDTO> toDashboardRentalDTOList(
            List<RentalEntity> rentals
    ) {
        return rentals.stream()
                .map(this::toDashboardRentalDTO)
                .toList();
    }

    private Page<DashboardBookDTO> getAvailableBooks(Pageable pageable) {
        return bookRepository.findByQuantityGreaterThan(0, pageable)
                .map(this::toDashboardBookDTO);
    }

    private DashboardBookDTO toDashboardBookDTO(BookEntity book) {
        return new DashboardBookDTO(
                book.getId(),
                book.getTitle(),
                book.getQuantity()
        );
    }

    private DashboardRentalDTO toDashboardRentalDTO(RentalEntity rental) {
        return new DashboardRentalDTO(
                rental.getId(),
                rental.getBooksId().getId(),
                rental.getBooksId().getTitle(),
                rental.getStartDate(),
                rental.getEndDate(),
                calculateStatus(rental)
        );
    }

    private String calculateStatus(RentalEntity rental) {

        if (isReturned(rental)) {
            return "RETURNED";
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = rental.getEndDate();

        if (endDate.isBefore(today)) {
            return "OVERDUE";
        }

        if (!endDate.isAfter(today.plusDays(2))) {
            return "NEAR_DUE";
        }

        return "ON_TIME";
    }

    private boolean isReturned(RentalEntity rental) {
        return Boolean.TRUE.equals(rental.getWasReturned())
                || "RETURNED".equalsIgnoreCase(rental.getStatus());
    }

    private Map<Long, Long> countActiveRentalsByBook(
            List<RentalEntity> rentals
    ) {
        Map<Long, Long> bookCounts = new HashMap<>();

        for (RentalEntity rental : rentals) {

            if (isReturned(rental)) {
                continue;
            }

            Long bookId = rental.getBooksId().getId();

            bookCounts.merge(bookId, 1L, Long::sum);
        }

        return bookCounts.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<Long, Long>comparingByValue()
                                .reversed()
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));
    }

    private MostRentedBookDTO findMostRentedBook(
            List<RentalEntity> rentals
    ) {
        if (rentals.isEmpty()) {
            return null;
        }

        Map<Long, Long> bookCounts = new HashMap<>();
        Map<Long, BookEntity> books = new HashMap<>();

        for (RentalEntity rental : rentals) {

            BookEntity book = rental.getBooksId();
            Long bookId = book.getId();

            bookCounts.merge(bookId, 1L, Long::sum);
            books.put(bookId, book);
        }

        Long mostRentedId = bookCounts.entrySet()
                .stream()
                .sorted(
                        Map.Entry.<Long, Long>comparingByValue()
                                .reversed()
                                .thenComparing(Map.Entry.comparingByKey())
                )
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (mostRentedId == null) {
            return null;
        }

        BookEntity book = books.get(mostRentedId);

        String publisher = book.getPublisherId() == null
                ? null
                : book.getPublisherId().getName();

        return new MostRentedBookDTO(
                book.getId(),
                book.getTitle(),
                publisher,
                book.getReleaseDate(),
                bookCounts.get(mostRentedId)
        );
    }
}
