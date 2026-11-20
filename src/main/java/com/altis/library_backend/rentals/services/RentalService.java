
package com.altis.library_backend.rentals.services;

import com.altis.library_backend.books.models.entities.BookEntity;
import com.altis.library_backend.books.repositories.BookRepository;
import com.altis.library_backend.rentals.models.dtos.RentalRequestDTO;
import com.altis.library_backend.rentals.models.dtos.RentalResponseDTO;
import com.altis.library_backend.rentals.models.dtos.RentalUpdateDTO;
import com.altis.library_backend.rentals.models.entities.RentalEntity;
import com.altis.library_backend.rentals.repositories.RentalRepository;
import com.altis.library_backend.rentals.specifications.RentalSpecification;
import com.altis.library_backend.users.models.entities.UserEntity;
import com.altis.library_backend.users.repositories.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public RentalService(
            RentalRepository rentalRepository,
            UserRepository userRepository,
            BookRepository bookRepository
    ) {
        this.rentalRepository = rentalRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public RentalResponseDTO findById(Long id) {

        RentalEntity rental = findRentalOrThrow(id);

        return toResponseDTO(rental);
    }

    @Transactional(readOnly = true)
    public Page<RentalResponseDTO> findAll(
            String userName,
            String bookTitle,
            Pageable pageable
    ) {

        Specification<RentalEntity> specification =
                RentalSpecification.hasUserName(userName)
                        .and(RentalSpecification.hasBookTitle(bookTitle));

        Page<RentalEntity> rentals =
                rentalRepository.findAll(specification, pageable);

        return rentals.map(this::toResponseDTO);
    }

    @Transactional
    public RentalResponseDTO createRental(RentalRequestDTO request) {

        if (request.usersId() == null || request.booksId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "User ID and book ID are required."
            );
        }

        UserEntity user = findUserOrThrow(request.usersId());

        validateUser(user);

        BookEntity book = findBookOrThrow(request.booksId());

        validateBookAvailability(book);

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(14);

        RentalEntity rental = new RentalEntity();

        rental.setUsersId(user);
        rental.setBooksId(book);
        rental.setStartDate(startDate);
        rental.setEndDate(endDate);
        rental.setStatus("ACTIVE");

        book.setQuantity(book.getQuantity() - 1);

        bookRepository.save(book);

        RentalEntity savedRental = rentalRepository.save(rental);

        return toResponseDTO(savedRental);
    }

    @Transactional
    public RentalResponseDTO updateRental(
            Long id,
            RentalUpdateDTO request
    ) {

        RentalEntity rental = findRentalOrThrow(id);

        validateRentalNotReturned(rental);

        if (request == null ||
                (request.usersId() == null && request.booksId() == null)) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please provide a user ID or book ID to update."
            );
        }

        Long currentUserId = rental.getUsersId().getId();
        Long currentBookId = rental.getBooksId().getId();

        boolean sameUser =
                request.usersId() == null ||
                        request.usersId().equals(currentUserId);

        boolean sameBook =
                request.booksId() == null ||
                        request.booksId().equals(currentBookId);

        if (sameUser && sameBook) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The provided data is the same as the current rental."
            );
        }

        if (request.usersId() != null && !sameUser) {

            UserEntity newUser =
                    findUserOrThrow(request.usersId());

            validateUser(newUser);

            rental.setUsersId(newUser);
        }

        if (request.booksId() != null && !sameBook) {

            BookEntity newBook =
                    findBookOrThrow(request.booksId());

            validateBookAvailability(newBook);

            BookEntity oldBook = rental.getBooksId();

            oldBook.setQuantity(oldBook.getQuantity() + 1);

            newBook.setQuantity(newBook.getQuantity() - 1);

            bookRepository.save(oldBook);
            bookRepository.save(newBook);

            rental.setBooksId(newBook);
        }

        RentalEntity savedRental =
                rentalRepository.save(rental);

        return toResponseDTO(savedRental);
    }

    @Transactional
    public RentalResponseDTO renewRental(Long id) {

        RentalEntity rental = findRentalOrThrow(id);

        validateRentalNotReturned(rental);

        rental.setEndDate(
                rental.getEndDate().plusDays(14)
        );

        RentalEntity savedRental =
                rentalRepository.save(rental);

        return toResponseDTO(savedRental);
    }

    @Transactional
    public RentalResponseDTO returnRental(Long id) {

        RentalEntity rental = findRentalOrThrow(id);

        validateRentalNotReturned(rental);

        rental.setStatus("RETURNED");

        BookEntity book = rental.getBooksId();

        book.setQuantity(book.getQuantity() + 1);

        bookRepository.save(book);

        RentalEntity savedRental =
                rentalRepository.save(rental);

        return toResponseDTO(savedRental);
    }

    @Transactional
    public void deleteRental(Long id) {

        RentalEntity rental = findRentalOrThrow(id);

        if (!"RETURNED".equals(rental.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only returned rentals can be deleted."
            );
        }

        rentalRepository.delete(rental);
    }

    private RentalEntity findRentalOrThrow(Long id) {

        return rentalRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Rental not found with ID: " + id
                        )
                );
    }

    private UserEntity findUserOrThrow(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with ID: " + id
                        )
                );
    }

    private BookEntity findBookOrThrow(Long id) {

        return bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Book not found with ID: " + id
                        )
                );
    }

    private void validateUser(UserEntity user) {

        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin users cannot rent books."
            );
        }
    }

    private void validateBookAvailability(BookEntity book) {

        if (book.getQuantity() == null || book.getQuantity() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Book is not available for rental."
            );
        }
    }

    private void validateRentalNotReturned(RentalEntity rental) {

        if ("RETURNED".equals(rental.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Rental has already been returned."
            );
        }
    }

    private String getRentalStatus(RentalEntity rental) {

        if ("RETURNED".equals(rental.getStatus())) {
            return "RETURNED";
        }

        LocalDate today = LocalDate.now();
        LocalDate endDate = rental.getEndDate();

        if (today.isAfter(endDate)) {
            return "LATE";
        }

        if (!today.isBefore(endDate.minusDays(2))) {
            return "DUE_SOON";
        }

        return "ACTIVE";
    }

    private RentalResponseDTO toResponseDTO(RentalEntity rental) {

        return new RentalResponseDTO(
                rental.getId(),
                rental.getUsersId().getId(),
                rental.getBooksId().getId(),
                rental.getStartDate(),
                rental.getEndDate(),
                getRentalStatus(rental)
        );
    }
}
