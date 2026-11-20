
package com.altis.library_backend.books.services;

import com.altis.library_backend.books.models.dtos.BookRequestDTO;
import com.altis.library_backend.books.models.dtos.BookResponseDTO;
import com.altis.library_backend.books.models.dtos.UpdateRequestDTO;
import com.altis.library_backend.books.models.dtos.UpdateResponseDTO;
import com.altis.library_backend.books.models.entities.BookEntity;
import com.altis.library_backend.books.repositories.BookRepository;
import com.altis.library_backend.books.specifications.BookSpecification;
import com.altis.library_backend.publishers.models.entities.PublisherEntity;
import com.altis.library_backend.publishers.repositories.PublisherRepository;
import com.altis.library_backend.rentals.repositories.RentalRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final PublisherRepository publisherRepository;
    private final RentalRepository rentalRepository;

    public BookService(
            BookRepository bookRepository,
            PublisherRepository publisherRepository,
            RentalRepository rentalRepository
    ) {
        this.bookRepository = bookRepository;
        this.publisherRepository = publisherRepository;
        this.rentalRepository = rentalRepository;
    }

    @Transactional(readOnly = true)
    public BookResponseDTO findById(Long id) {

        BookEntity findBook = bookRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Book not found with ID: " + id
                        )
                );

        return toBookResponse(findBook);
    }

    @Transactional(readOnly = true)
    public Page<BookResponseDTO> findAll(
            String title,
            String genre,
            String isbn,
            Pageable pageable
    ) {

        Specification<BookEntity> spec =
                BookSpecification.hasTitle(title)
                        .and(BookSpecification.hasGenre(genre))
                        .and(BookSpecification.hasIsbn(isbn));

        Page<BookEntity> books =
                bookRepository.findAll(spec, pageable);

        return books.map(this::toBookResponse);
    }

    @Transactional
    public BookResponseDTO createBook(BookRequestDTO request) {

        validateQuantity(request.quantity());

        BookEntity existingBook =
                bookRepository.findByIsbn(request.isbn())
                        .orElse(null);

        if (existingBook != null) {

            if (!existingBook.getTitle().equals(request.title())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ISBN is registered with a different title."
                );
            }

            if (!existingBook.getGenre().equals(request.genre())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ISBN is registered with a different genre."
                );
            }

            if (!existingBook.getReleaseDate().equals(request.releaseDate())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ISBN is registered with a different release date."
                );
            }

            if (!existingBook.getPublisherId().getId()
                    .equals(request.publisherId())) {

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "This ISBN is registered with a different publisher."
                );
            }

            int currentQuantity =
                    existingBook.getQuantity() == null
                            ? 0
                            : existingBook.getQuantity();

            long newQuantity =
                    (long) currentQuantity + request.quantity();

            if (newQuantity > Integer.MAX_VALUE) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Book quantity exceeds the allowed limit."
                );
            }

            existingBook.setQuantity((int) newQuantity);

            BookEntity savedBook =
                    bookRepository.save(existingBook);

            return toBookResponse(savedBook);
        }

        if (request.publisherId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Publisher ID is required."
            );
        }

        PublisherEntity publisher =
                publisherRepository.findById(request.publisherId())
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Publisher not found with ID: "
                                                + request.publisherId()
                                )
                        );

        BookEntity newBook = new BookEntity();

        newBook.setIsbn(request.isbn());
        newBook.setTitle(request.title());
        newBook.setGenre(request.genre());
        newBook.setReleaseDate(request.releaseDate());
        newBook.setPublisherId(publisher);

        // Quantidade informada no cadastro
        newBook.setQuantity(request.quantity());

        BookEntity savedBook =
                bookRepository.save(newBook);

        return toBookResponse(savedBook);
    }

    @Transactional
    public UpdateResponseDTO updateBook(
            Long id,
            UpdateRequestDTO request
    ) {

        BookEntity existingBook =
                bookRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Book not found with ID: " + id
                                )
                        );

        if (request.title() != null
                && !request.title().isBlank()) {

            existingBook.setTitle(request.title());
        }

        if (request.genre() != null
                && !request.genre().isBlank()) {

            existingBook.setGenre(request.genre());
        }

        if (request.releaseDate() != null) {

            existingBook.setReleaseDate(request.releaseDate());
        }

        if (request.publisherId() != null) {

            PublisherEntity publisher =
                    publisherRepository.findById(request.publisherId())
                            .orElseThrow(() ->
                                    new ResponseStatusException(
                                            HttpStatus.NOT_FOUND,
                                            "Publisher not found with ID: "
                                                    + request.publisherId()
                                    )
                            );

            existingBook.setPublisherId(publisher);
        }

        // Permite editar a quantidade disponível
        if (request.quantity() != null) {

            validateQuantity(request.quantity());

            existingBook.setQuantity(request.quantity());
        }

        BookEntity savedBook =
                bookRepository.save(existingBook);

        return new UpdateResponseDTO(
                savedBook.getId(),
                savedBook.getTitle(),
                savedBook.getGenre(),
                savedBook.getReleaseDate(),
                savedBook.getPublisherId().getId(),
                savedBook.getPublisherId().getName(),
                savedBook.getQuantity()
        );
    }

    @Transactional
    public void deleteBook(Long id) {

        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Book not found with ID: " + id
            );
        }

        if (rentalRepository.existsByBooksId_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "There are rentals registered with this book."
            );
        }

        bookRepository.deleteById(id);
    }

    private void validateQuantity(Integer quantity) {

        if (quantity == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book quantity is required."
            );
        }

        if (quantity < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Book quantity cannot be negative."
            );
        }
    }

    private BookResponseDTO toBookResponse(BookEntity book) {

        return new BookResponseDTO(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getGenre(),
                book.getReleaseDate(),
                book.getPublisherId().getId(),
                book.getPublisherId().getName(),
                book.getQuantity()
        );
    }
}
