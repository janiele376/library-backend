package com.altis.library_backend.publishers.services;

import com.altis.library_backend.books.repositories.BookRepository;
import com.altis.library_backend.publishers.models.dtos.PublisherRequestDTO;
import com.altis.library_backend.publishers.models.dtos.PublisherResponseDTO;
import com.altis.library_backend.publishers.models.dtos.UpdateRequestDTO;
import com.altis.library_backend.publishers.models.dtos.UpdateResponseDTO;
import com.altis.library_backend.publishers.models.entities.PublisherEntity;
import com.altis.library_backend.publishers.repositories.PublisherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublisherService {

    private final PublisherRepository publishersRepository;
    private final BookRepository bookRepository;

    public PublisherService(
            PublisherRepository publishersRepository,
            BookRepository bookRepository
    ) {
        this.publishersRepository = publishersRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public PublisherResponseDTO findById(Long id) {

        PublisherEntity findPublisher = publishersRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Publisher not found with ID: " + id
                        )
                );

        return new PublisherResponseDTO(
                findPublisher.getId(),
                findPublisher.getName(),
                findPublisher.getCnpj(),
                findPublisher.getEmail(),
                findPublisher.getPhone(),
                findPublisher.getAddress()
        );
    }


    @Transactional(readOnly = true)
    public Page<PublisherResponseDTO> findAll(
            String name,
            Pageable pageable
    ) {
        Page<PublisherEntity> publishers;

        if (name != null && !name.isBlank()) {
            publishers = publishersRepository
                    .findByNameContainingIgnoreCase(
                            name.trim(),
                            pageable
                    );
        } else {
            publishers = publishersRepository.findAll(pageable);
        }

        return publishers.map(publisher ->
                new PublisherResponseDTO(
                        publisher.getId(),
                        publisher.getName(),
                        publisher.getCnpj(),
                        publisher.getEmail(),
                        publisher.getPhone(),
                        publisher.getAddress()
                )
        );
    }


    @Transactional
    public PublisherResponseDTO createPublisher(
            PublisherRequestDTO request
    ) {

        if (publishersRepository.existsByCnpj(request.cnpj())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The CNPJ is already in use."
            );
        }

        if (publishersRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The email is already in use."
            );
        }

        PublisherEntity newPublisher = new PublisherEntity();

        newPublisher.setName(request.name());
        newPublisher.setCnpj(request.cnpj());
        newPublisher.setEmail(request.email());
        newPublisher.setPhone(request.phone());
        newPublisher.setAddress(request.address());

        PublisherEntity savedPublishers =
                publishersRepository.save(newPublisher);

        return new PublisherResponseDTO(
                savedPublishers.getId(),
                savedPublishers.getName(),
                savedPublishers.getCnpj(),
                savedPublishers.getEmail(),
                savedPublishers.getPhone(),
                savedPublishers.getAddress()
        );
    }

    @Transactional
    public UpdateResponseDTO updatePublisher(
            Long id,
            UpdateRequestDTO request
    ) {

        PublisherEntity existingPublisher =
                publishersRepository.findById(id)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Publisher not found with ID: " + id
                                )
                        );

        if (request.email() != null
                && !request.email().isBlank()
                && !existingPublisher.getEmail().equals(request.email())
                && publishersRepository.existsByEmail(request.email())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email already in use by another publisher."
            );
        }

        if (request.name() != null
                && !request.name().isBlank()) {

            existingPublisher.setName(
                    request.name()
            );
        }

        if (request.email() != null
                && !request.email().isBlank()) {

            existingPublisher.setEmail(
                    request.email()
            );
        }

        if (request.phone() != null
                && !request.phone().isBlank()) {

            existingPublisher.setPhone(
                    request.phone()
            );
        }

        if (request.address() != null
                && !request.address().isBlank()) {

            existingPublisher.setAddress(
                    request.address()
            );
        }

        PublisherEntity savedPublisher =
                publishersRepository.save(existingPublisher);

        return new UpdateResponseDTO(
                savedPublisher.getId(),
                savedPublisher.getName(),
                savedPublisher.getEmail(),
                savedPublisher.getPhone(),
                savedPublisher.getAddress()
        );
    }

    @Transactional
    public void deletePublisher(Long id) {

        if (!publishersRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Publisher not found with ID: " + id
            );
        }

        if (bookRepository.existsByPublisherId_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "There are books registered with this publisher."
            );
        }

        publishersRepository.deleteById(id);
    }
}